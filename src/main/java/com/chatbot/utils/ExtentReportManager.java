package com.chatbot.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.chatbot.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * ExtentReportManager — Thread-safe singleton for Extent Reports.
 * Creates an HTML report with system info and logs test results.
 */
public class ExtentReportManager {

    private static final Logger log = LogManager.getLogger(ExtentReportManager.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    private ExtentReportManager() {}

    /**
     * Initialises the Extent Reports instance.
     * Call once before the test suite starts.
     */
    public static synchronized ExtentReports getExtentReports() {
        if (extentReports == null) {
            String reportPath = config.getExtentReportPath();

            // Ensure output directory exists
            new File(reportPath).getParentFile().mkdirs();

            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setDocumentTitle("AI Chatbot Automation Report");
            sparkReporter.config().setReportName("Chatbot Test Execution Report");
            sparkReporter.config().setTimelineEnabled(true);
            sparkReporter.config().setEncoding("UTF-8");

            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);

            // System information shown in report
            extentReports.setSystemInfo("Framework",    "AI Chatbot Testing Framework v1.0");
            extentReports.setSystemInfo("Environment",  config.getEnvironment());
            extentReports.setSystemInfo("Browser",      config.getBrowser());
            extentReports.setSystemInfo("Base URL",     config.getAppBaseUrl());
            extentReports.setSystemInfo("API URL",      config.getApiBaseUrl());
            extentReports.setSystemInfo("OS",           System.getProperty("os.name"));
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("Tester",       System.getProperty("user.name"));

            log.info("ExtentReports initialised → {}", reportPath);
        }
        return extentReports;
    }

    /** Creates a new test entry in the current thread. */
    public static ExtentTest createTest(String testName, String description) {
        ExtentTest test = getExtentReports().createTest(testName, description);
        extentTest.set(test);
        return test;
    }

    /** Returns the ExtentTest for the current thread. */
    public static ExtentTest getTest() {
        return extentTest.get();
    }

    /** Logs an INFO message to the current test. */
    public static void logInfo(String message) {
        if (getTest() != null) getTest().info(message);
    }

    /** Logs a PASS result. */
    public static void logPass(String message) {
        if (getTest() != null) getTest().pass(message);
    }

    /** Logs a FAIL result with screenshot. */
    public static void logFail(String message, String testName) {
        if (getTest() != null) {
            String screenshotPath = ScreenshotUtil.captureScreenshot(testName);
            getTest().fail(message);
            if (screenshotPath != null) {
                getTest().addScreenCaptureFromPath(screenshotPath, "Failure Screenshot");
            }
        }
    }

    /** Logs a SKIP result. */
    public static void logSkip(String message) {
        if (getTest() != null) getTest().skip(message);
    }

    /** Logs with arbitrary status. */
    public static void log(Status status, String message) {
        if (getTest() != null) getTest().log(status, message);
    }

    /** Flushes all results to disk. Call in @AfterSuite. */
    public static void flush() {
        if (extentReports != null) {
            extentReports.flush();
            log.info("ExtentReports flushed.");
        }
    }
}
