package com.chatbot.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * RetryAnalyzer — Retries a flaky test up to MAX_RETRY_COUNT times.
 *
 * Usage — annotate individual tests:
 *   @Test(retryAnalyzer = RetryAnalyzer.class)
 *
 * Or apply globally via the TestListener:
 *   result.getMethod().setRetryAnalyzer(new RetryAnalyzer());
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY_COUNT = 2;  // retry up to 2 extra times (3 total attempts)

    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            log.warn("Retrying FAILED test '{}' — attempt {}/{}",
                result.getName(), retryCount, MAX_RETRY_COUNT);

            // Reset result to allow retry
            result.setStatus(ITestResult.FAILURE);
            return true;
        }
        if (retryCount >= MAX_RETRY_COUNT) {
            log.error("Test '{}' failed after {} attempts — marking as FAILED.",
                result.getName(), MAX_RETRY_COUNT + 1);
        }
        return false;
    }

    public int getRetryCount()    { return retryCount; }
    public int getMaxRetryCount() { return MAX_RETRY_COUNT; }
}
