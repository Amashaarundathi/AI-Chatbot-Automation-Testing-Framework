package com.chatbot.api;

import com.chatbot.config.ConfigManager;
import com.chatbot.utils.ApiUtil;
import com.chatbot.utils.ExtentReportManager;
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * ChatBotApiTest — REST Assured API test suite.
 *
 * Covers:
 *  - Health check endpoint
 *  - Valid chat request (status, body, response time, JSON schema)
 *  - Data-driven message validation
 *  - Missing/null fields in request body
 *  - Large payload
 *  - API timeout handling
 *  - HTTP method validation (GET on POST endpoint)
 *  - Content-Type validation
 */
@Epic("AI Chatbot Testing")
@Feature("API Automation")
public class ChatBotApiTest extends ApiBaseTest {

    private static final Logger log = LogManager.getLogger(ChatBotApiTest.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    @BeforeClass
    public void classSetup() {
        ExtentReportManager.createTest("ChatBotApiTest", "REST API test suite for chatbot backend");
    }

    // ── 1. Health Check ───────────────────────────────────────────────────────

    @Test(description = "Verify chatbot health endpoint returns 200 OK",
          groups = {"smoke", "api"}, priority = 1)
    @Story("Health Check")
    @Severity(SeverityLevel.BLOCKER)
    public void testHealthEndpointReturns200() {
        ExtentReportManager.logInfo("GET " + config.getHealthEndpoint());
        Response response = ApiUtil.get(config.getHealthEndpoint());

        Assert.assertEquals(response.getStatusCode(), 200,
            "Health endpoint should return 200 OK.");
        Assert.assertTrue(ApiUtil.assertResponseTime(response, 10000),
            "Health check response time should be < 10000ms.");

        ExtentReportManager.logPass("Health endpoint returned 200 in " +
            ApiUtil.getResponseTimeMs(response) + "ms.");
    }

    // ── 2. Valid Chat Request ─────────────────────────────────────────────────

    @Test(description = "Verify valid chat API request returns 200 with non-empty response",
          groups = {"smoke", "api"}, priority = 2)
    @Story("Valid Chat Request")
    @Severity(SeverityLevel.CRITICAL)
    public void testValidChatRequest() {
        Map<String, Object> body = buildChatRequest("Hello, how are you?");
        ExtentReportManager.logInfo("POST /chat with message: 'Hello, how are you?'");

        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        Assert.assertEquals(response.getStatusCode(), 200,
            "Valid request should return 200.");
        Assert.assertFalse(response.getBody().asString().isEmpty(),
            "Response body should not be empty.");
        Assert.assertTrue(ApiUtil.assertResponseTime(response, config.getAcceptableResponseMs()),
            "Response time should be within acceptable threshold.");

        String responseText = ApiUtil.extractField(response, "response");
        Assert.assertNotNull(responseText, "Response JSON should contain 'response' field.");
        Assert.assertFalse(responseText.isEmpty(), "'response' field should not be empty.");

        ExtentReportManager.logPass("Valid chat request returned 200 with response: " + responseText);
    }

    // ── 3. JSON Schema Validation ─────────────────────────────────────────────

    @Test(description = "Verify chat API response matches expected JSON schema",
          groups = {"api"}, priority = 3)
    @Story("Schema Validation")
    @Severity(SeverityLevel.NORMAL)
    public void testChatResponseSchema() {
        Map<String, Object> body = buildChatRequest("Schema validation test");
        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        Assert.assertEquals(response.getStatusCode(), 200);

        File schemaFile = new File("src/test/resources/schemas/chat-response-schema.json");
        if (schemaFile.exists()) {
            response.then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchema(schemaFile));
            ExtentReportManager.logPass("JSON schema validation passed.");
        } else {
            log.warn("Schema file not found — skipping schema assertion.");
            ExtentReportManager.logInfo("Schema file not found — skipped schema assertion.");

            // Validate key fields are present instead
            Assert.assertNotNull(ApiUtil.extractField(response, "response"),
                "Response body should contain 'response' field.");
            Assert.assertNotNull(ApiUtil.extractField(response, "sessionId"),
                "Response body should contain 'sessionId' field.");
        }
    }

    // ── 4. Response Time ──────────────────────────────────────────────────────

    @Test(description = "Verify API response time is within acceptable threshold",
          groups = {"api", "performance"}, priority = 4)
    @Story("Response Time")
    @Severity(SeverityLevel.NORMAL)
    public void testResponseTimeWithinThreshold() {
        Map<String, Object> body = buildChatRequest("Performance timing test");
        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        long responseTime = ApiUtil.getResponseTimeMs(response);
        long threshold    = config.getAcceptableResponseMs();

        log.info("Response time: {}ms | Threshold: {}ms", responseTime, threshold);
        ExtentReportManager.logInfo("Response time: " + responseTime + "ms | Threshold: " + threshold + "ms");

        Assert.assertTrue(responseTime <= threshold,
            String.format("Response time %dms exceeds threshold %dms", responseTime, threshold));
        ExtentReportManager.logPass("Response time within threshold: " + responseTime + "ms");
    }

    // ── 5. Data-Driven Message Validation ────────────────────────────────────

    @Test(description = "Verify API handles various message types correctly",
          dataProvider = "apiMessages",
          groups = {"functional", "api"})
    @Story("Message Validation")
    @Severity(SeverityLevel.NORMAL)
    public void testVariousMessageTypes(String message, int expectedStatus) {
        ExtentReportManager.logInfo("Testing message: " + message);
        Map<String, Object> body = buildChatRequest(message);
        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        Assert.assertEquals(response.getStatusCode(), expectedStatus,
            "Unexpected status for message: " + message);
        ExtentReportManager.logPass("Message '" + message + "' → HTTP " + response.getStatusCode());
    }

    @DataProvider(name = "apiMessages")
    public Object[][] apiMessagesProvider() {
        return new Object[][]{
            {"Hello",                      200},
            {"What is today's date?",      200},
            {"Tell me about your features",200},
            {"How do I reset my password?",200},
            {"I need technical support",   200},
        };
    }

    // ── 6. Missing Message Field ──────────────────────────────────────────────

    @Test(description = "Verify API returns 400 when message field is missing",
          groups = {"negative", "api"})
    @Story("Missing Fields")
    @Severity(SeverityLevel.NORMAL)
    public void testMissingMessageField() {
        Map<String, Object> body = new HashMap<>();
        body.put("sessionId", "test-session-123");
        // Intentionally omit "message"

        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        Assert.assertTrue(
            response.getStatusCode() == 400 || response.getStatusCode() == 422,
            "API should return 400/422 when message is missing. Got: " + response.getStatusCode()
        );
        ExtentReportManager.logPass("Missing message field correctly returned " + response.getStatusCode());
    }

    // ── 7. Null / Empty Message ───────────────────────────────────────────────

    @Test(description = "Verify API handles null and empty message values",
          groups = {"negative", "api"})
    @Story("Empty Input")
    @Severity(SeverityLevel.NORMAL)
    public void testEmptyMessage() {
        Map<String, Object> body = buildChatRequest("");
        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        Assert.assertTrue(
            response.getStatusCode() == 400 || response.getStatusCode() == 422,
            "Empty message should return 400/422. Got: " + response.getStatusCode()
        );
        ExtentReportManager.logPass("Empty message handled correctly: HTTP " + response.getStatusCode());
    }

    // ── 8. Null Request Body ──────────────────────────────────────────────────

    @Test(description = "Verify API handles a completely empty/null request body",
          groups = {"negative", "api"})
    @Story("Null Body")
    @Severity(SeverityLevel.NORMAL)
    public void testNullRequestBody() {
        Response response = ApiUtil.post(config.getChatEndpoint(), "{}");

        Assert.assertTrue(
            response.getStatusCode() >= 400,
            "Null/empty body should return 4xx. Got: " + response.getStatusCode()
        );
        ExtentReportManager.logPass("Null body returned: HTTP " + response.getStatusCode());
    }

    // ── 9. Large Payload ──────────────────────────────────────────────────────

    @Test(description = "Verify API handles oversized payloads gracefully",
          groups = {"negative", "api"})
    @Story("Large Payload")
    @Severity(SeverityLevel.MINOR)
    public void testLargePayload() {
        String largeMessage = "A".repeat(100_000); // 100KB
        Map<String, Object> body = buildChatRequest(largeMessage);
        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        Assert.assertTrue(
            response.getStatusCode() == 413 ||
            response.getStatusCode() == 400 ||
            response.getStatusCode() == 200,
            "API should gracefully handle large payload. Got: " + response.getStatusCode()
        );
        ExtentReportManager.logPass("Large payload returned HTTP " + response.getStatusCode());
    }

    // ── 10. Wrong HTTP Method ─────────────────────────────────────────────────

    @Test(description = "Verify chat endpoint rejects GET requests",
          groups = {"negative", "api"})
    @Story("Method Validation")
    @Severity(SeverityLevel.MINOR)
    public void testWrongHttpMethod() {
        Response response = ApiUtil.get(config.getChatEndpoint());

        Assert.assertTrue(
            response.getStatusCode() == 405 || response.getStatusCode() == 404,
            "GET on POST-only endpoint should return 405/404. Got: " + response.getStatusCode()
        );
        ExtentReportManager.logPass("Wrong HTTP method correctly rejected: HTTP " + response.getStatusCode());
    }

    // ── 11. Invalid Content-Type ──────────────────────────────────────────────

    @Test(description = "Verify API rejects plain-text Content-Type",
          groups = {"negative", "api"})
    @Story("Content-Type Validation")
    @Severity(SeverityLevel.MINOR)
    public void testInvalidContentType() {
        Response response = RestAssured.given()
            .spec(ApiUtil.authSpec())
            .contentType("text/plain")
            .body("hello this is plain text")
            .post(config.getChatEndpoint());

        Assert.assertTrue(
            response.getStatusCode() == 415 || response.getStatusCode() == 400,
            "Plain-text content type should return 415/400. Got: " + response.getStatusCode()
        );
        ExtentReportManager.logPass("Invalid content type rejected: HTTP " + response.getStatusCode());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> buildChatRequest(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("sessionId", "test-session-" + System.currentTimeMillis());
        body.put("userId", "qa-automation-user");
        return body;
    }
}
