package com.framework.tests.ui;

import com.framework.pages.InventoryPage;
import com.framework.pages.LoginPage;
import com.framework.tests.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Login feature test suite.
 *
 * Demonstrates:
 * - Allure annotations (@Epic, @Feature, @Story, @Severity)
 * - Data-driven testing with @DataProvider
 * - Fluent POM usage
 * - AssertJ for readable assertions
 */
@Epic("Authentication")
@Feature("Login")
public class LoginTest extends BaseTest {

    // ─── Happy path ───────────────────────────────────────────────────────────

    @Test(description = "Standard user can log in successfully")
    @Story("Valid login")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that a standard user can authenticate and reach the inventory page")
    public void standardUserCanLogin() {
        InventoryPage inventoryPage = new LoginPage()
                .open()
                .loginAs("standard_user", "secret_sauce");

        assertThat(inventoryPage.isLoaded())
                .as("Inventory page should be loaded after successful login")
                .isTrue();

        assertThat(inventoryPage.getPageTitle())
                .as("Page title should be 'Products'")
                .isEqualTo("Products");
    }

    @Test(description = "Logged-in user can log out and return to login page")
    @Story("Logout")
    @Severity(SeverityLevel.CRITICAL)
    public void userCanLogout() {
        LoginPage loginPage = new LoginPage()
                .open()
                .loginAs("standard_user", "secret_sauce")
                .logout();

        assertThat(loginPage.isLoaded())
                .as("Login page should be visible after logout")
                .isTrue();
    }

    // ─── Negative / error path ────────────────────────────────────────────────

    @Test(description = "Invalid credentials show an error message")
    @Story("Invalid login")
    @Severity(SeverityLevel.CRITICAL)
    public void invalidCredentialsShowError() {
        LoginPage loginPage = new LoginPage()
                .open()
                .enterUsername("wrong_user")
                .enterPassword("wrong_pass")
                .clickLoginExpectingError();

        assertThat(loginPage.isErrorDisplayed())
                .as("Error message should be visible")
                .isTrue();

        assertThat(loginPage.getErrorMessage())
                .as("Error should mention username/password mismatch")
                .contains("Username and password do not match");
    }

    @Test(description = "Empty credentials show validation error")
    @Story("Invalid login")
    @Severity(SeverityLevel.NORMAL)
    public void emptyCredentialsShowValidationError() {
        LoginPage loginPage = new LoginPage()
                .open()
                .clickLoginExpectingError();

        assertThat(loginPage.isErrorDisplayed()).isTrue();
        assertThat(loginPage.getErrorMessage()).contains("Username is required");
    }

    // ─── Data-driven ──────────────────────────────────────────────────────────

    @Test(
        dataProvider = "lockedOutUsers",
        description  = "Locked-out users cannot log in"
    )
    @Story("Locked user")
    @Severity(SeverityLevel.NORMAL)
    public void lockedOutUserCannotLogin(String username, String password) {
        LoginPage loginPage = new LoginPage()
                .open()
                .enterUsername(username)
                .enterPassword(password)
                .clickLoginExpectingError();

        assertThat(loginPage.isErrorDisplayed()).isTrue();
        assertThat(loginPage.getErrorMessage())
                .contains("Sorry, this user has been locked out");
    }

    @DataProvider(name = "lockedOutUsers")
    public Object[][] lockedOutUsersProvider() {
        return new Object[][] {
            { "locked_out_user", "secret_sauce" }
        };
    }
}
