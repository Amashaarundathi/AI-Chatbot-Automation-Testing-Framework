package com.chatbot.ui;

import com.chatbot.utils.ExtentReportManager;
import com.chatbot.utils.ScreenshotUtil;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;

/**
 * TestListener — Hooks into TestNG lifecycle to integrate Allure
 * and Extent Reports automatically for every test.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        log.info("[LISTENER] Test started: {}", result.getName());
        ExtentReportManager.logInfo("Test started: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("[LISTENER] Test PASSED: {}", result.getName());
        ExtentReportManager.logPass("✅ " + result.getName() + " — PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("[LISTENER] Test FAILED: {} | Reason: {}",
            result.getName(),
            result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown");

        // Screenshot → Allure
        byte[] bytes = ScreenshotUtil.captureAsBytes();
        if (bytes.length > 0) {
            Allure.addAttachment(
                "Failure Screenshot — " + result.getName(),
                "image/png",
                new ByteArrayInputStream(bytes),
                ".png"
            );
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("[LISTENER] Test SKIPPED: {}", result.getName());
        ExtentReportManager.logSkip("⚠️ " + result.getName() + " — SKIPPED");
    }

    @Override
    public void onStart(ITestContext context) {
        log.info("[LISTENER] Suite started: {}", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("[LISTENER] Suite finished: {} | Pass: {} | Fail: {} | Skip: {}",
            context.getName(),
            context.getPassedTests().size(),
            context.getFailedTests().size(),
            context.getSkippedTests().size());
        ExtentReportManager.flush();
    }
}
