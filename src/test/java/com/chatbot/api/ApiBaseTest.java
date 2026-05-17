package com.chatbot.api;

import com.chatbot.config.ConfigManager;
import com.chatbot.mock.MockChatbotServer;
import com.chatbot.models.ChatRequest;
import com.chatbot.utils.ExtentReportManager;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class ApiBaseTest {

    protected static final Logger log = LogManager.getLogger(ApiBaseTest.class);
    protected static final ConfigManager config = ConfigManager.getInstance();

    @BeforeSuite(alwaysRun = true)
    public void apiSuiteSetup() {
        MockChatbotServer.start();
        RestAssured.baseURI = MockChatbotServer.BASE_URL;
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        ExtentReportManager.getExtentReports();
        log.info("Mock server started. Base URI: {}", MockChatbotServer.BASE_URL);
    }

    @AfterSuite(alwaysRun = true)
    public void apiSuiteTeardown() {
        ExtentReportManager.flush();
        MockChatbotServer.stop();
        log.info("Mock server stopped.");
    }

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
