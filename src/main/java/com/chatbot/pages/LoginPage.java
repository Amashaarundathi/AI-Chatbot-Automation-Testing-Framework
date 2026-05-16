package com.chatbot.pages;

import com.chatbot.utils.WaitUtil;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * LoginPage — Page Object for the chatbot application login screen.
 *
 * Update locators to match your application's HTML.
 */
public class LoginPage {

    private static final Logger log = LogManager.getLogger(LoginPage.class);
    private final WebDriver driver;

    // ── Locators — update these ───────────────────────────────────────────────
    private final By emailField      = By.cssSelector("input[type='email'], #email, input[name='email']");
    private final By passwordField   = By.cssSelector("input[type='password'], #password");
    private final By loginButton     = By.cssSelector("button[type='submit'], #login-btn, .login-button");
    private final By errorMessage    = By.cssSelector(".error-message, .login-error, [data-testid='login-error']");
    private final By successRedirect = By.cssSelector(".chat-container, #chat-window, .dashboard");
    private final By logoutButton    = By.cssSelector("#logout, .logout-btn, [data-testid='logout']");
    private final By forgotPassword  = By.cssSelector("a[href*='forgot'], .forgot-password");
    private final By rememberMe      = By.cssSelector("input[name='remember'], #remember-me");
    // ─────────────────────────────────────────────────────────────────────────

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @Step("Navigate to login page: {url}")
    public LoginPage navigateTo(String url) {
        log.info("Navigating to login: {}", url);
        driver.get(url);
        WaitUtil.waitForVisible(driver, emailField);
        return this;
    }

    @Step("Enter email: {email}")
    public LoginPage enterEmail(String email) {
        log.info("Entering email: {}", email);
        WebElement field = WaitUtil.waitForClickable(driver, emailField);
        field.clear();
        field.sendKeys(email);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        WebElement field = WaitUtil.waitForClickable(driver, passwordField);
        field.clear();
        field.sendKeys(password);
        return this;
    }

    @Step("Click login button")
    public LoginPage clickLogin() {
        log.info("Clicking login button.");
        WaitUtil.waitForClickable(driver, loginButton).click();
        return this;
    }

    @Step("Login with credentials: {email}")
    public ChatBotPage loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        WaitUtil.waitForVisible(driver, successRedirect, 15);
        log.info("Login successful for: {}", email);
        return new ChatBotPage(driver);
    }

    @Step("Attempt login with invalid credentials")
    public LoginPage loginWithInvalidCredentials(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
        return this;
    }

    @Step("Click Forgot Password")
    public LoginPage clickForgotPassword() {
        WaitUtil.waitForClickable(driver, forgotPassword).click();
        return this;
    }

    // ── State Checks ──────────────────────────────────────────────────────────

    /** True if the login error message is visible. */
    public boolean isErrorDisplayed() {
        try { return driver.findElement(errorMessage).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    /** Returns the error message text. */
    public String getErrorMessage() {
        try { return WaitUtil.waitForVisible(driver, errorMessage, 5).getText().trim(); }
        catch (Exception e) { return ""; }
    }

    /** True if the login form is visible. */
    public boolean isLoginFormVisible() {
        try { return driver.findElement(emailField).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    /** True if the user has been redirected to the chat/dashboard. */
    public boolean isLoggedIn() {
        try { return driver.findElement(successRedirect).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    @Step("Logout")
    public LoginPage logout() {
        try {
            WaitUtil.waitForClickable(driver, logoutButton).click();
            WaitUtil.waitForVisible(driver, emailField, 10);
            log.info("Logged out successfully.");
        } catch (Exception e) {
            log.warn("Logout button not found: {}", e.getMessage());
        }
        return this;
    }
}
