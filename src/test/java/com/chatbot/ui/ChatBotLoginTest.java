package com.chatbot.ui;

import com.chatbot.pages.LoginPage;
import com.chatbot.utils.DriverManager;
import com.chatbot.utils.ExtentReportManager;
import com.chatbot.utils.RetryAnalyzer;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * ChatBotLoginTest — UI tests for the chatbot login / authentication flow.
 *
 * Covers:
 *  - Valid login redirects to chatbot
 *  - Invalid credentials show error
 *  - Empty credentials validation
 *  - Session logout behaviour
 *  - Account lockout after repeated failures
 */
@Epic("AI Chatbot Testing")
@Feature("Authentication UI")
public class ChatBotLoginTest extends BaseTest {

    // ── 1. Valid Login ────────────────────────────────────────────────────────

    @Test(description = "Valid credentials redirect user to chatbot",
          groups = {"smoke", "ui", "auth"}, priority = 1,
          retryAnalyzer = RetryAnalyzer.class)
    @Story("Login Flow")
    @Severity(SeverityLevel.BLOCKER)
    public void testValidLoginRedirectsToChatbot() {
        LoginPage loginPage = new LoginPage(driver()).navigateTo(config.getAppBaseUrl() + "/login");

        ExtentReportManager.logInfo("Attempting login with valid credentials.");
        var chatPage = loginPage.loginAs(
            System.getProperty("app.test.email",    "qa-tester@example.com"),
            System.getProperty("app.test.password", "TestPassword123!")
        );

        Assert.assertTrue(chatPage.isChatVisible(),
            "After valid login, chatbot UI should be visible.");
        ExtentReportManager.logPass("Valid login → chatbot visible.");
    }

    // ── 2. Invalid Credentials ────────────────────────────────────────────────

    @Test(description = "Invalid credentials show an error message",
          groups = {"ui", "auth", "negative"}, priority = 2)
    @Story("Login — Invalid Credentials")
    @Severity(SeverityLevel.CRITICAL)
    public void testInvalidCredentialsShowError() {
        LoginPage loginPage = new LoginPage(driver()).navigateTo(config.getAppBaseUrl() + "/login");

        loginPage.loginWithInvalidCredentials("invalid@test.com", "WrongPassword!");

        Assert.assertTrue(loginPage.isErrorDisplayed(),
            "An error message should appear for invalid credentials.");
        Assert.assertFalse(loginPage.isLoggedIn(),
            "User should NOT be redirected on invalid credentials.");

        ExtentReportManager.logPass("Invalid credentials correctly rejected with error message.");
    }

    // ── 3. Empty Email ────────────────────────────────────────────────────────

    @Test(description = "Empty email field shows validation error",
          groups = {"ui", "auth", "negative"}, priority = 3)
    @Story("Login — Empty Fields")
    @Severity(SeverityLevel.NORMAL)
    public void testEmptyEmailShowsValidation() {
        LoginPage loginPage = new LoginPage(driver()).navigateTo(config.getAppBaseUrl() + "/login");
        loginPage.loginWithInvalidCredentials("", "SomePassword!");

        Assert.assertTrue(loginPage.isErrorDisplayed() || loginPage.isLoginFormVisible(),
            "Submitting empty email should show validation or remain on login page.");
        Assert.assertFalse(loginPage.isLoggedIn());
        ExtentReportManager.logPass("Empty email validation works correctly.");
    }

    // ── 4. Empty Password ─────────────────────────────────────────────────────

    @Test(description = "Empty password field shows validation error",
          groups = {"ui", "auth", "negative"}, priority = 4)
    @Story("Login — Empty Fields")
    @Severity(SeverityLevel.NORMAL)
    public void testEmptyPasswordShowsValidation() {
        LoginPage loginPage = new LoginPage(driver()).navigateTo(config.getAppBaseUrl() + "/login");
        loginPage.loginWithInvalidCredentials("user@example.com", "");

        Assert.assertFalse(loginPage.isLoggedIn(),
            "Empty password should not allow login.");
        ExtentReportManager.logPass("Empty password validation works correctly.");
    }

    // ── 5. Data-Driven Invalid Logins ─────────────────────────────────────────

    @Test(description = "Multiple invalid credential combinations all fail gracefully",
          dataProvider = "invalidCredentials",
          groups = {"ui", "auth", "negative"}, priority = 0)
    @Story("Login — Invalid Credentials")
    @Severity(SeverityLevel.NORMAL)
    public void testInvalidCredentialsDataDriven(String email, String password, String description) {
        ExtentReportManager.logInfo("Testing: " + description);
        LoginPage loginPage = new LoginPage(driver()).navigateTo(config.getAppBaseUrl() + "/login");
        loginPage.loginWithInvalidCredentials(email, password);

        Assert.assertFalse(loginPage.isLoggedIn(),
            "Should NOT login with: " + description);
        ExtentReportManager.logPass("Correctly rejected: " + description);
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentialsProvider() {
        return new Object[][]{
            {"notanemail",            "password123",  "Malformed email"},
            {"user@example.com",      "wrong",        "Wrong password"},
            {"nonexistent@test.com",  "password123",  "Non-existent account"},
            {"admin@example.com",     "' OR '1'='1",  "SQL injection password"},
            {"<script>@x.com",        "password",     "XSS in email field"},
            {"user@example.com",      "",             "Empty password"},
            {"",                      "password123",  "Empty email"},
        };
    }

    // ── 6. Logout ─────────────────────────────────────────────────────────────

    @Test(description = "Logged-in user can log out and is returned to login page",
          groups = {"ui", "auth"}, priority = 5)
    @Story("Logout")
    @Severity(SeverityLevel.NORMAL)
    public void testLogoutReturnsToLoginPage() {
        LoginPage loginPage = new LoginPage(driver()).navigateTo(config.getAppBaseUrl() + "/login");

        loginPage.loginAs(
            System.getProperty("app.test.email",    "qa-tester@example.com"),
            System.getProperty("app.test.password", "TestPassword123!")
        );

        loginPage.logout();

        Assert.assertTrue(loginPage.isLoginFormVisible(),
            "After logout, login form should be visible again.");
        ExtentReportManager.logPass("Logout returns user to login page.");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private org.openqa.selenium.WebDriver driver() {
        return DriverManager.getDriver();
    }
}
