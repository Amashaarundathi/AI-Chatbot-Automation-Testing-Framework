package com.chatbot.ui;

import com.chatbot.config.ConfigManager;
import com.chatbot.mock.MockChatbotServer;
import com.chatbot.pages.ChatBotPage;
import com.chatbot.utils.DriverManager;
import com.chatbot.utils.ExtentReportManager;
import com.chatbot.utils.ScreenshotUtil;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

/**
 * BaseTest — Parent class for all UI test classes.
 *
 * Responsibilities:
 *  - Initialise / quit WebDriver per test method (thread-safe).
 *  - Create ExtentTest node for each test.
 *  - Capture screenshot and attach to Allure + Extent on failure.
 *  - Expose shared page objects to sub-classes.
 */
@Listeners({com.chatbot.ui.TestListener.class})
public class BaseTest {

    protected static final Logger log = LogManager.getLogger(BaseTest.class);
    protected static final ConfigManager config = ConfigManager.getInstance();
    protected ChatBotPage chatBotPage;

    // ── Suite Level ───────────────────────────────────────────────────────────

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        log.info("========== TEST SUITE STARTING ==========");
        MockChatbotServer.start();
        log.info("Mock server started on {}", MockChatbotServer.BASE_URL);
        ExtentReportManager.getExtentReports();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        ExtentReportManager.flush();
        MockChatbotServer.stop();
        log.info("========== TEST SUITE COMPLETE ==========");
    }

    // ── Class Level — one browser per test class ──────────────────────────────

    @BeforeClass(alwaysRun = true)
    public void setUpClass() {
        DriverManager.initDriver();
        chatBotPage = new ChatBotPage(DriverManager.getDriver());
        DriverManager.getDriver().get(config.getAppBaseUrl());
        log.info("Browser opened for class: {}", getClass().getSimpleName());
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        DriverManager.quitDriver();
        log.info("Browser closed for class: {}", getClass().getSimpleName());
    }

    // ── Test Level ────────────────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void setUp(java.lang.reflect.Method method) {
        log.info("------ Starting test: {} ------", method.getName());
        // Refresh page before each test to reset chat state
        DriverManager.getDriver().navigate().refresh();
        chatBotPage = new ChatBotPage(DriverManager.getDriver());
        Test testAnnotation = method.getAnnotation(Test.class);
        String description = (testAnnotation != null) ? testAnnotation.description() : "";
        ExtentReportManager.createTest(method.getName(), description);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("TEST FAILED: {}", result.getName());
            byte[] screenshot = ScreenshotUtil.captureAsBytes();
            if (screenshot.length > 0) {
                Allure.addAttachment("Failure Screenshot", "image/png",
                    new ByteArrayInputStream(screenshot), ".png");
            }
            ExtentReportManager.logFail(
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Test failed",
                result.getName()
            );
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            log.info("TEST PASSED: {}", result.getName());
            ExtentReportManager.logPass("Test passed successfully.");
        } else if (result.getStatus() == ITestResult.SKIP) {
            log.warn("TEST SKIPPED: {}", result.getName());
            ExtentReportManager.logSkip("Test skipped.");
        }
        log.info("------ Finished test: {} ------", result.getName());
    }

}
