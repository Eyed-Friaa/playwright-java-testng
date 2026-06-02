package com.framework.tests.ui;

import com.framework.pages.InventoryPage;
import com.framework.pages.LoginPage;
import com.framework.tests.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Inventory / product listing tests.
 */
@Epic("Shop")
@Feature("Product Listing")
public class InventoryTest extends BaseTest {

    private InventoryPage inventoryPage;

    @BeforeMethod
    public void loginBeforeTest() {
        inventoryPage = new LoginPage()
                .open()
                .loginAs("standard_user", "secret_sauce");
    }

    @Test(description = "Inventory page displays 6 products")
    @Story("Product display")
    @Severity(SeverityLevel.CRITICAL)
    public void inventoryDisplaysSixProducts() {
        assertThat(inventoryPage.getProductCount())
                .as("Should display exactly 6 products")
                .isEqualTo(6);
    }

    @Test(description = "Adding item to cart increments badge")
    @Story("Cart")
    @Severity(SeverityLevel.BLOCKER)
    public void addingItemIncrementsCartBadge() {
        assertThat(inventoryPage.getCartBadgeCount()).isZero();

        inventoryPage.addFirstItemToCart();

        assertThat(inventoryPage.getCartBadgeCount())
                .as("Cart badge should show 1 after adding one item")
                .isEqualTo(1);
    }

    @Test(description = "Products can be sorted A-Z")
    @Story("Sorting")
    @Severity(SeverityLevel.NORMAL)
    public void productsSortedAlphabetically() {
        inventoryPage.sortBy("az");
        List<String> names = inventoryPage.getProductNames();

        assertThat(names)
                .as("Products should be sorted A-Z")
                .isSortedAccordingTo(String::compareToIgnoreCase);
    }

    @Test(description = "Products can be sorted Z-A")
    @Story("Sorting")
    @Severity(SeverityLevel.NORMAL)
    public void productsSortedReverseAlphabetically() {
        inventoryPage.sortBy("za");
        List<String> names = inventoryPage.getProductNames();

        List<String> expected = names.stream()
                .sorted((a, b) -> b.compareToIgnoreCase(a))
                .toList();

        assertThat(names)
                .as("Products should be sorted Z-A")
                .isEqualTo(expected);
    }
}
