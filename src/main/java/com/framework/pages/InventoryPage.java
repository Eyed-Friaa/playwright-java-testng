package com.framework.pages;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Page Object for SauceDemo inventory (products) page.
 */
@Slf4j
public class InventoryPage extends BasePage {

    private static final String PAGE_TITLE       = ".title";
    private static final String INVENTORY_ITEMS  = ".inventory_item";
    private static final String ITEM_NAME        = ".inventory_item_name";
    private static final String ITEM_PRICE       = ".inventory_item_price";
    private static final String ADD_TO_CART_BTN  = "[data-test^='add-to-cart']";
    private static final String CART_BADGE       = ".shopping_cart_badge";
    private static final String SORT_DROPDOWN    = "[data-test='product_sort_container']";
    private static final String BURGER_MENU      = "#react-burger-menu-btn";
    private static final String LOGOUT_LINK      = "#logout_sidebar_link";

    // ─── Page validation ──────────────────────────────────────────────────────

    @Override
    public boolean isLoaded() {
        return isVisible(PAGE_TITLE) && getCurrentUrl().contains("inventory");
    }

    public String getPageTitle() {
        return getText(PAGE_TITLE);
    }

    // ─── Product interactions ─────────────────────────────────────────────────

    @Step("Get all product names")
    public List<String> getProductNames() {
        return locator(ITEM_NAME).allInnerTexts();
    }

    @Step("Get all product prices")
    public List<String> getProductPrices() {
        return locator(ITEM_PRICE).allInnerTexts();
    }

    @Step("Get product count")
    public int getProductCount() {
        return getElementCount(INVENTORY_ITEMS);
    }

    @Step("Add first product to cart")
    public InventoryPage addFirstItemToCart() {
        locator(ADD_TO_CART_BTN).first().click();
        log.info("Added first item to cart");
        return this;
    }

    @Step("Add product '{productName}' to cart")
    public InventoryPage addItemToCart(String productName) {
        String selector = String.format(
            "//div[contains(@class,'inventory_item_name') and text()='%s']" +
            "/ancestor::div[@class='inventory_item']//button", productName);
        locator(selector).click();
        log.info("Added '{}' to cart", productName);
        return this;
    }

    // ─── Cart ─────────────────────────────────────────────────────────────────

    public int getCartBadgeCount() {
        if (!isVisible(CART_BADGE)) return 0;
        return Integer.parseInt(getText(CART_BADGE));
    }

    // ─── Sorting ──────────────────────────────────────────────────────────────

    @Step("Sort products by: {option}")
    public InventoryPage sortBy(String option) {
        selectOption(SORT_DROPDOWN, option);
        return this;
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @Step("Logout")
    public LoginPage logout() {
        click(BURGER_MENU);
        waitForVisible(LOGOUT_LINK);
        click(LOGOUT_LINK);
        return new LoginPage();
    }
}
