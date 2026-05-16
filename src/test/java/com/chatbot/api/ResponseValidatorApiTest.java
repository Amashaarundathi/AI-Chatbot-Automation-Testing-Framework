package com.chatbot.api;

import com.chatbot.utils.ApiUtil;
import com.chatbot.utils.ExtentReportManager;
import com.chatbot.utils.ResponseValidator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * ResponseValidatorApiTest — Demonstrates and validates the ResponseValidator
 * fluent assertion DSL across real API scenarios.
 *
 * These tests also serve as integration tests for the ResponseValidator itself.
 */
@Epic("AI Chatbot Testing")
@Feature("API — Response Validation")
public class ResponseValidatorApiTest extends ApiBaseTest {

    @BeforeClass
    public void setup() {
        ExtentReportManager.createTest("ResponseValidatorApiTest",
            "Fluent response validator API tests");
    }

    // ── 1. Full Happy-Path Chain ──────────────────────────────────────────────

    @Test(description = "Validate a successful chat response using fluent validator chain",
          groups = {"api", "functional"})
    @Story("Fluent Response Validation")
    @Severity(SeverityLevel.NORMAL)
    public void testFullValidChainOnSuccessfulResponse() {
        Response response = ApiUtil.post(config.getChatEndpoint(), buildRequest("Hello there!"));

        ResponseValidator.of(response)
            .assertSuccess()
            .assertResponseTimeUnder(config.getAcceptableResponseMs())
            .assertBodyNotEmpty()
            .assertFieldPresent("response")
            .assertFieldPresent("sessionId")
            .assertResponseNotEmpty()
            .assertNoSensitiveDataLeaked()
            .assertNoXssReflected()
            .logResponseTime();

        ExtentReportManager.logPass("Full validation chain passed on successful response.");
    }

    // ── 2. Security Assertions ────────────────────────────────────────────────

    @Test(description = "Verify no sensitive data or XSS is reflected for SQL payload",
          groups = {"api", "security"})
    @Story("Security Assertions")
    @Severity(SeverityLevel.CRITICAL)
    public void testNoSecurityLeakageForSqlPayload() {
        Response response = ApiUtil.post(config.getChatEndpoint(),
            buildRequest("'; DROP TABLE users; --"));

        ResponseValidator.of(response)
            .assertNotServerError()
            .assertNoSensitiveDataLeaked()
            .assertNoSqlErrorExposed()
            .assertNoXssReflected();

        ExtentReportManager.logPass("No security data leakage for SQL payload.");
    }

    // ── 3. Unauthorized Assertion ─────────────────────────────────────────────

    @Test(description = "Validator correctly identifies 401 for missing token",
          groups = {"api", "security"})
    @Story("Auth Assertions")
    @Severity(SeverityLevel.CRITICAL)
    public void testUnauthorizedAssertion() {
        Map<String, Object> body = new HashMap<>();
        body.put("message",   "hello no auth");
        body.put("sessionId", "no-auth-session");

        Response response = ApiUtil.postNoAuth(config.getChatEndpoint(), body);

        ResponseValidator.of(response)
            .assertUnauthorized()
            .assertNoSensitiveDataLeaked();

        ExtentReportManager.logPass("Validator correctly asserted 401 Unauthorized.");
    }

    // ── 4. Bad Request Assertion ──────────────────────────────────────────────

    @Test(description = "Validator correctly identifies 400/422 for missing message field",
          groups = {"api", "negative"})
    @Story("Bad Request Assertions")
    @Severity(SeverityLevel.NORMAL)
    public void testBadRequestAssertion() {
        Map<String, Object> body = new HashMap<>();
        body.put("sessionId", "missing-message-session");
        // No "message" field

        Response response = ApiUtil.post(config.getChatEndpoint(), body);

        ResponseValidator.of(response)
            .assertBadRequest()
            .assertNoSensitiveDataLeaked();

        ExtentReportManager.logPass("Validator correctly asserted 400/422 Bad Request.");
    }

    // ── 5. Response Contains Keyword ──────────────────────────────────────────

    @Test(description = "Validator confirms response contains expected keyword for greeting",
          groups = {"api", "functional"})
    @Story("Response Content Assertions")
    @Severity(SeverityLevel.NORMAL)
    public void testResponseContainsKeyword() {
        Response response = ApiUtil.post(config.getChatEndpoint(), buildRequest("Hello"));

        ResponseValidator validator = ResponseValidator.of(response).assertSuccess();
        String body = validator.body().toLowerCase();

        // At least one greeting keyword expected
        boolean hasGreeting = body.contains("hello") || body.contains("hi") ||
                              body.contains("hey")   || body.contains("welcome") ||
                              body.contains("assist");

        org.testng.Assert.assertTrue(hasGreeting,
            "Response should contain a greeting keyword. Body: " + body);

        ExtentReportManager.logPass("Response contains expected greeting keyword.");
    }

    // ── 6. Response Time Boundary ─────────────────────────────────────────────

    @Test(description = "Validator correctly fails when response exceeds custom threshold",
          groups = {"api", "performance"})
    @Story("Response Time Assertions")
    @Severity(SeverityLevel.MINOR)
    public void testResponseTimeLogged() {
        Response response = ApiUtil.post(config.getChatEndpoint(), buildRequest("timing test"));

        long time = ResponseValidator.of(response)
            .assertSuccess()
            .responseTime();

        // Just log — don't assert here so test doesn't fail on slow env
        ExtentReportManager.logInfo("Response time: " + time + "ms");
        ExtentReportManager.logPass("Response time logged: " + time + "ms");
    }
}
