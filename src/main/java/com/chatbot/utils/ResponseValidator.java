package com.chatbot.utils;

import com.chatbot.models.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * ResponseValidator — Fluent, reusable assertion library for chatbot API responses.
 *
 * Usage:
 *   ResponseValidator.of(response)
 *       .assertStatus(200)
 *       .assertResponseTimeUnder(3000)
 *       .assertBodyNotEmpty()
 *       .assertFieldPresent("response")
 *       .assertResponseContains("hello")
 *       .assertNoSensitiveDataLeaked();
 */
public class ResponseValidator {

    private static final Logger log = LogManager.getLogger(ResponseValidator.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Response response;
    private ChatResponse chatResponse;

    private ResponseValidator(Response response) {
        this.response = response;
        tryDeserialize();
    }

    public static ResponseValidator of(Response response) {
        return new ResponseValidator(response);
    }

    private void tryDeserialize() {
        try {
            this.chatResponse = mapper.readValue(response.getBody().asString(), ChatResponse.class);
        } catch (Exception e) {
            log.debug("Could not deserialize to ChatResponse (may be an error body): {}", e.getMessage());
        }
    }

    // ── Status Assertions ─────────────────────────────────────────────────────

    public ResponseValidator assertStatus(int expected) {
        int actual = response.getStatusCode();
        log.info("Asserting status code: expected={}, actual={}", expected, actual);
        Assert.assertEquals(actual, expected,
            "Expected HTTP " + expected + " but got " + actual +
            ". Body: " + response.getBody().asString().substring(0, Math.min(200, response.getBody().asString().length())));
        return this;
    }

    public ResponseValidator assertStatusIn(int... allowedStatuses) {
        int actual = response.getStatusCode();
        boolean found = false;
        for (int s : allowedStatuses) if (s == actual) { found = true; break; }
        Assert.assertTrue(found,
            "Status " + actual + " not in allowed set: " + Arrays.toString(allowedStatuses));
        return this;
    }

    public ResponseValidator assertSuccess() {
        return assertStatus(200);
    }

    public ResponseValidator assertClientError() {
        int code = response.getStatusCode();
        Assert.assertTrue(code >= 400 && code < 500,
            "Expected a 4xx client error, got: " + code);
        return this;
    }

    public ResponseValidator assertNotServerError() {
        int code = response.getStatusCode();
        Assert.assertTrue(code < 500,
            "Unexpected server error (5xx): " + code +
            ". Body: " + response.getBody().asString());
        return this;
    }

    // ── Timing Assertions ─────────────────────────────────────────────────────

    public ResponseValidator assertResponseTimeUnder(long maxMs) {
        long actual = ApiUtil.getResponseTimeMs(response);
        log.info("Response time: {}ms (max {}ms)", actual, maxMs);
        Assert.assertTrue(actual <= maxMs,
            String.format("Response time %dms exceeded threshold %dms", actual, maxMs));
        return this;
    }

    public ResponseValidator logResponseTime() {
        log.info("Response time: {}ms", ApiUtil.getResponseTimeMs(response));
        return this;
    }

    // ── Body Assertions ───────────────────────────────────────────────────────

    public ResponseValidator assertBodyNotEmpty() {
        String body = response.getBody().asString();
        Assert.assertFalse(body == null || body.trim().isEmpty(),
            "Response body should not be empty.");
        return this;
    }

    public ResponseValidator assertFieldPresent(String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        Assert.assertNotNull(value,
            "Expected field '" + jsonPath + "' to be present in response.");
        return this;
    }

    public ResponseValidator assertFieldEquals(String jsonPath, Object expectedValue) {
        Object actual = response.jsonPath().get(jsonPath);
        Assert.assertEquals(actual, expectedValue,
            "Field '" + jsonPath + "': expected=" + expectedValue + ", actual=" + actual);
        return this;
    }

    public ResponseValidator assertFieldNotEmpty(String jsonPath) {
        Object val = response.jsonPath().get(jsonPath);
        Assert.assertNotNull(val, "Field '" + jsonPath + "' should not be null.");
        Assert.assertFalse(val.toString().trim().isEmpty(),
            "Field '" + jsonPath + "' should not be empty.");
        return this;
    }

    // ── Response Content Assertions ───────────────────────────────────────────

    public ResponseValidator assertResponseContains(String keyword) {
        if (chatResponse == null || chatResponse.getResponse() == null) {
            assertBodyContains(keyword);
            return this;
        }
        Assert.assertTrue(chatResponse.responseContains(keyword),
            "Bot response should contain '" + keyword + "'. Got: " + chatResponse.getResponse());
        return this;
    }

    public ResponseValidator assertResponseNotEmpty() {
        if (chatResponse != null) {
            Assert.assertTrue(chatResponse.hasResponse(),
                "Bot response field should not be empty.");
        } else {
            assertBodyNotEmpty();
        }
        return this;
    }

    public ResponseValidator assertBodyContains(String text) {
        String body = response.getBody().asString();
        Assert.assertTrue(body.toLowerCase().contains(text.toLowerCase()),
            "Response body should contain '" + text + "'. Body: " + body);
        return this;
    }

    public ResponseValidator assertBodyNotContains(String text) {
        String body = response.getBody().asString().toLowerCase();
        Assert.assertFalse(body.contains(text.toLowerCase()),
            "Response body should NOT contain '" + text + "'.");
        return this;
    }

    // ── Security Assertions ───────────────────────────────────────────────────

    public ResponseValidator assertNoSensitiveDataLeaked() {
        List<String> sensitiveTerms = Arrays.asList(
            "password", "secret", "api_key", "apikey", "private_key",
            "stack trace", "exception", "caused by", "at com.", "at org.",
            "syntax error", "sql error", "mysql", "postgresql", "mongodb",
            "internal server error", "debug", "stacktrace"
        );
        String body = response.getBody().asString().toLowerCase();
        for (String term : sensitiveTerms) {
            Assert.assertFalse(body.contains(term),
                "Response leaks sensitive data — contains: '" + term + "'");
        }
        log.info("Sensitive data leakage check PASSED.");
        return this;
    }

    public ResponseValidator assertNoXssReflected() {
        String body = response.getBody().asString();
        Assert.assertFalse(body.contains("<script>"),    "Response reflects <script> tag.");
        Assert.assertFalse(body.contains("onerror="),    "Response reflects onerror attribute.");
        Assert.assertFalse(body.contains("javascript:"), "Response reflects javascript: URI.");
        log.info("XSS reflection check PASSED.");
        return this;
    }

    public ResponseValidator assertNoSqlErrorExposed() {
        String body = response.getBody().asString().toLowerCase();
        Assert.assertFalse(body.contains("sql") && body.contains("error"),
            "Response appears to expose SQL error details.");
        Assert.assertFalse(body.contains("syntax error near"),
            "Response exposes SQL syntax error.");
        log.info("SQL error exposure check PASSED.");
        return this;
    }

    // ── Status Alias Checks ───────────────────────────────────────────────────

    public ResponseValidator assertUnauthorized() { return assertStatus(401); }
    public ResponseValidator assertForbidden()    { return assertStatus(403); }
    public ResponseValidator assertBadRequest()   { return assertStatusIn(400, 422); }
    public ResponseValidator assertNotFound()     { return assertStatus(404); }

    // ── Access Raw Response ───────────────────────────────────────────────────

    public Response         raw()          { return response; }
    public ChatResponse     asChatResponse(){ return chatResponse; }
    public String           body()         { return response.getBody().asString(); }
    public int              status()       { return response.getStatusCode(); }
    public long             responseTime() { return ApiUtil.getResponseTimeMs(response); }
}
