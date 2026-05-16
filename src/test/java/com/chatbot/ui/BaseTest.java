package com.chatbot.ui;

import com.chatbot.config.ConfigManager;
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
        ExtentReportManager.getExtentReports(); // initialise report
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        ExtentReportManager.flush();
        log.info("========== TEST SUITE COMPLETE ==========");
    }

    // ── Test Level ────────────────────────────────────────────────────────────

    @BeforeMethod(alwaysRun = true)
    public void setUp(java.lang.reflect.Method method) {
        log.info("------ Starting test: {} ------", method.getName());
        DriverManager.initDriver();
        chatBotPage = new ChatBotPage(DriverManager.getDriver());

        // Navigate to the app
        DriverManager.getDriver().get(config.getAppBaseUrl());

        // Create an Extent test node
        Test testAnnotation = method.getAnnotation(Test.class);
        String description = (testAnnotation != null) ? testAnnotation.description() : "";
        ExtentReportManager.createTest(method.getName(), description);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // Capture screenshot on failure
        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("TEST FAILED: {}", result.getName());

            // Attach to Allure
            byte[] screenshot = ScreenshotUtil.captureAsBytes();
            if (screenshot.length > 0) {
                Allure.addAttachment("Failure Screenshot", "image/png",
                    new ByteArrayInputStream(screenshot), ".png");
            }

            // Log to ExtentReports
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

        DriverManager.quitDriver();
        log.info("------ Finished test: {} ------", result.getName());
    }

}
