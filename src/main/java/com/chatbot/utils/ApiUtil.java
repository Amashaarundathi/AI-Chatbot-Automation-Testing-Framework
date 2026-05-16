package com.chatbot.utils;

import com.chatbot.config.ConfigManager;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ApiUtil — Reusable REST-Assured wrapper for chatbot API testing.
 * Provides authenticated and unauthenticated request builders,
 * plus convenience methods for GET / POST / response assertions.
 */
public class ApiUtil {

    private static final Logger log = LogManager.getLogger(ApiUtil.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    private ApiUtil() {}

    // ── Base Specification ─────────────────────────────────────────────────────

    /**
     * Default request spec: base URL, JSON content type, Allure + log filters.
     */
    public static RequestSpecification baseSpec() {
        return new RequestSpecBuilder()
            .setBaseUri(config.getApiBaseUrl())
            .setContentType(ContentType.JSON)
            .addFilter(new AllureRestAssured())
            .addFilter(new RequestLoggingFilter())
            .addFilter(new ResponseLoggingFilter())
            .build();
    }

    /**
     * Authenticated spec — includes a valid Bearer token.
     */
    public static RequestSpecification authSpec() {
        return new RequestSpecBuilder()
            .addRequestSpecification(baseSpec())
            .addHeader("Authorization", config.getValidToken())
            .build();
    }

    /**
     * Spec with a custom token (invalid / expired testing).
     */
    public static RequestSpecification specWithToken(String token) {
        return new RequestSpecBuilder()
            .addRequestSpecification(baseSpec())
            .addHeader("Authorization", token)
            .build();
    }

    // ── HTTP Methods ───────────────────────────────────────────────────────────

    /** POST with JSON body and auth. */
    public static Response post(String endpoint, Object body) {
        log.info("POST {} | body: {}", endpoint, body);
        return RestAssured.given()
            .spec(authSpec())
            .body(body)
            .post(endpoint);
    }

    /** POST without authentication. */
    public static Response postNoAuth(String endpoint, Object body) {
        log.info("POST (no-auth) {} | body: {}", endpoint, body);
        return RestAssured.given()
            .spec(baseSpec())
            .body(body)
            .post(endpoint);
    }

    /** POST with a custom token. */
    public static Response postWithToken(String endpoint, Object body, String token) {
        log.info("POST (custom-token) {}", endpoint);
        return RestAssured.given()
            .spec(specWithToken(token))
            .body(body)
            .post(endpoint);
    }

    /** GET with auth. */
    public static Response get(String endpoint) {
        log.info("GET {}", endpoint);
        return RestAssured.given()
            .spec(authSpec())
            .get(endpoint);
    }

    /** GET with query params. */
    public static Response get(String endpoint, Map<String, Object> queryParams) {
        log.info("GET {} | params: {}", endpoint, queryParams);
        return RestAssured.given()
            .spec(authSpec())
            .queryParams(queryParams)
            .get(endpoint);
    }

    // ── Response Helpers ──────────────────────────────────────────────────────

    /**
     * Returns the response time in milliseconds.
     */
    public static long getResponseTimeMs(Response response) {
        return response.getTimeIn(TimeUnit.MILLISECONDS);
    }

    /**
     * Extracts a JSON field value by path.
     */
    public static <T> T extractField(Response response, String jsonPath) {
        return response.jsonPath().get(jsonPath);
    }

    /**
     * Asserts status code and logs mismatch.
     */
    public static boolean assertStatusCode(Response response, int expected) {
        int actual = response.getStatusCode();
        if (actual != expected) {
            log.error("Status code mismatch — expected: {}, actual: {}", expected, actual);
            return false;
        }
        log.info("Status code assertion passed: {}", actual);
        return true;
    }

    /**
     * Asserts that response time is within threshold.
     */
    public static boolean assertResponseTime(Response response, long maxMs) {
        long actual = getResponseTimeMs(response);
        if (actual > maxMs) {
            log.warn("Response time exceeded threshold — actual: {}ms, max: {}ms", actual, maxMs);
            return false;
        }
        log.info("Response time OK: {}ms (max {}ms)", actual, maxMs);
        return true;
    }

    /**
     * Checks whether a response body contains a keyword (case-insensitive).
     */
    public static boolean responseContains(Response response, String keyword) {
        boolean found = response.getBody().asString().toLowerCase()
                               .contains(keyword.toLowerCase());
        log.info("Keyword '{}' in response: {}", keyword, found);
        return found;
    }
}
