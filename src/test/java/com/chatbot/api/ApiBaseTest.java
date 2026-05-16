package com.chatbot.api;

import com.chatbot.config.ConfigManager;
import com.chatbot.models.ChatRequest;
import com.chatbot.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * ApiBaseTest — Shared setup for all API test classes.
 *
 * Initialises RestAssured defaults and the Extent Report.
 * Extend this instead of duplicating @BeforeSuite logic.
 */
public class ApiBaseTest {

    protected static final Logger log = LogManager.getLogger(ApiBaseTest.class);
    protected static final ConfigManager config = ConfigManager.getInstance();

    @BeforeSuite(alwaysRun = true)
    public void apiSuiteSetup() {
        RestAssured.baseURI = config.getApiBaseUrl();
        RestAssured.defaultParser = Parser.JSON;

        // Enable request/response logging for failed tests only
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        ExtentReportManager.getExtentReports();
        log.info("API suite configured. Base URI: {}", config.getApiBaseUrl());
    }

    @AfterSuite(alwaysRun = true)
    public void apiSuiteTeardown() {
        ExtentReportManager.flush();
    }

    // ── Shared Request Builder ────────────────────────────────────────────────

    /**
     * Builds a standard ChatRequest with a unique session per call.
     */
    protected ChatRequest buildRequest(String message) {
        return ChatRequest.builder()
            .message(message)
            .sessionId("api-test-" + System.currentTimeMillis())
            .userId("api-tester")
            .build();
    }

    protected ChatRequest buildRequest(String message, String sessionId) {
        return ChatRequest.builder()
            .message(message)
            .sessionId(sessionId)
            .userId("api-tester")
            .build();
    }
}
