package com.framework.pages;

import com.framework.utils.BrowserManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

import static com.framework.config.ConfigManager.get;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Base class for all Page Objects.
 *
 * Design principles:
 * - Every interaction is wrapped for logging + Allure step attachment
 * - Locators are always fetched fresh (no caching) to avoid stale references
 * - Fluent API: most action methods return 'this' or the target page type
 * - Never use Thread.sleep() — use Playwright's built-in waiting mechanisms
 */
@Slf4j
public abstract class BasePage {

    protected final Page page;

    protected BasePage() {
        this.page = BrowserManager.getPage();
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @Step("Navigate to: {url}")
    protected void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        page.navigate(url);
        page.waitForLoadState();
    }

    protected void navigateToBaseUrl() {
        navigateTo(get().baseUrl());
    }

    // ─── Interactions ─────────────────────────────────────────────────────────

    @Step("Click element: {selector}")
    protected void click(String selector) {
        log.debug("Click: {}", selector);
        locator(selector).click();
    }

    @Step("Type '{text}' into: {selector}")
    protected void type(String selector, String text) {
        log.debug("Type '{}' into: {}", text, selector);
        Locator loc = locator(selector);
        loc.clear();
        loc.fill(text);
    }

    @Step("Select option '{value}' in: {selector}")
    protected void selectOption(String selector, String value) {
        locator(selector).selectOption(value);
    }

    @Step("Check checkbox: {selector}")
    protected void check(String selector) {
        locator(selector).check();
    }

    @Step("Hover over: {selector}")
    protected void hover(String selector) {
        locator(selector).hover();
    }

    // ─── Retrieval ────────────────────────────────────────────────────────────

    protected String getText(String selector) {
        return locator(selector).innerText().trim();
    }

    protected String getAttributeValue(String selector, String attribute) {
        return locator(selector).getAttribute(attribute);
    }

    protected boolean isVisible(String selector) {
        return locator(selector).isVisible();
    }

    protected boolean isEnabled(String selector) {
        return locator(selector).isEnabled();
    }

    protected int getElementCount(String selector) {
        return locator(selector).count();
    }

    // ─── Waiting ──────────────────────────────────────────────────────────────

    protected void waitForVisible(String selector) {
        locator(selector).waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }

    protected void waitForHidden(String selector) {
        locator(selector).waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN));
    }

    /**
     * Wait for DOM to be interactive. Prefer this over the (removed) network-idle
     * wait: SPAs with background polling never idle, making the old helper hang
     * until timeout. When you truly need "no in-flight requests", use
     * {@link Page#waitForResponse} scoped to the endpoint you care about.
     */
    protected void waitForDomReady() {
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    // ─── Web-first assertions ────────────────────────────────────────────────
    // Prefer these over isVisible()/getText() in tests — they auto-retry until
    // the framework timeout and produce far clearer failure messages.

    protected LocatorAssertions expect(Locator locator) {
        return assertThat(locator);
    }

    protected LocatorAssertions expect(String selector) {
        return assertThat(locator(selector));
    }

    protected PageAssertions expectPage() {
        return assertThat(page);
    }

    // ─── Page verification ────────────────────────────────────────────────────

    public String getTitle() {
        return page.title();
    }

    public String getCurrentUrl() {
        return page.url();
    }

    /**
     * Override in each page object to verify correct page is loaded.
     */
    public abstract boolean isLoaded();

    // ─── Visual regression / accessibility ────────────────────────────────────
    // Thin sugar so page objects can express intent without importing the utils.

    @Step("Visual check: {name}")
    protected void assertVisualMatches(String name) {
        com.framework.utils.VisualRegression.assertPageMatches(page, name);
    }

    @Step("Visual check element {selector}: {name}")
    protected void assertVisualMatches(String selector, String name) {
        com.framework.utils.VisualRegression.assertLocatorMatches(locator(selector), name);
    }

    @Step("Accessibility scan — no serious+ violations")
    protected void assertNoSeriousA11yViolations() {
        com.framework.utils.Accessibility.assertNoSeriousViolations(page);
    }

    // ─── Locator factory ──────────────────────────────────────────────────────

    /**
     * Returns a fresh Locator. Prefer semantic selectors:
     * getByRole, getByLabel, getByText, getByTestId over CSS/XPath.
     */
    protected Locator locator(String selector) {
        return page.locator(selector);
    }

    protected Locator getByTestId(String testId) {
        return page.getByTestId(testId);
    }

    protected Locator getByRole(com.microsoft.playwright.options.AriaRole role, String name) {
        return page.getByRole(role, new Page.GetByRoleOptions().setName(name));
    }
}
