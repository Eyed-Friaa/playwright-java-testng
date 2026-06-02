package com.framework.pages;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

/**
 * Page Object for SauceDemo login page.
 * Demonstrates full POM pattern with fluent API and Allure steps.
 */
@Slf4j
public class LoginPage extends BasePage {

    // ─── Selectors ────────────────────────────────────────────────────────────
    // Using data-test attributes (ideal) — always prefer over CSS classes

    private static final String USERNAME_INPUT  = "[data-test='username']";
    private static final String PASSWORD_INPUT  = "[data-test='password']";
    private static final String LOGIN_BUTTON    = "[data-test='login-button']";
    private static final String ERROR_MESSAGE   = "[data-test='error']";
    private static final String ERROR_CLOSE_BTN = ".error-button";

    // ─── Navigation ───────────────────────────────────────────────────────────

    @Step("Open login page")
    public LoginPage open() {
        navigateToBaseUrl();
        waitForVisible(LOGIN_BUTTON);
        log.info("Login page opened");
        return this;
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        type(USERNAME_INPUT, username);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        type(PASSWORD_INPUT, password);
        return this;
    }

    @Step("Click login button")
    public InventoryPage clickLogin() {
        click(LOGIN_BUTTON);
        return new InventoryPage();
    }

    @Step("Attempt login with invalid credentials")
    public LoginPage clickLoginExpectingError() {
        click(LOGIN_BUTTON);
        return this;
    }

    /**
     * Complete login flow — happy path.
     */
    @Step("Login as '{username}'")
    public InventoryPage loginAs(String username, String password) {
        return enterUsername(username)
                .enterPassword(password)
                .clickLogin();
    }

    // ─── Assertions helpers (used by test layer) ──────────────────────────────

    public boolean isErrorDisplayed() {
        return isVisible(ERROR_MESSAGE);
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    @Step("Dismiss error message")
    public LoginPage dismissError() {
        click(ERROR_CLOSE_BTN);
        return this;
    }

    @Override
    public boolean isLoaded() {
        return isVisible(LOGIN_BUTTON) && getCurrentUrl().contains("saucedemo.com");
    }
}
