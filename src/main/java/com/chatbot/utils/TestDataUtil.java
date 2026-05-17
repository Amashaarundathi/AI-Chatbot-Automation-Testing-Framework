package com.chatbot.utils;

import com.chatbot.config.ConfigManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * TestDataUtil — Loads JSON test data files and generates dynamic test data
 * using Java Faker for data-driven testing.
 */
public class TestDataUtil {

    private static final Logger log = LogManager.getLogger(TestDataUtil.class);
    private static final ConfigManager config = ConfigManager.getInstance();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Faker faker = new Faker();

    private TestDataUtil() {}

    // ── JSON Loaders ──────────────────────────────────────────────────────────

    /**
     * Loads a JSON file into a List of Maps (array of objects).
     */
    public static List<Map<String, Object>> loadJsonArray(String fileName) {
        try {
            File file = new File(config.getTestDataPath() + fileName);
            return mapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Failed to load test data file '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Test data load failure: " + fileName, e);
        }
    }

    /**
     * Loads a JSON file into a Map (single object).
     */
    public static Map<String, Object> loadJsonObject(String fileName) {
        try {
            File file = new File(config.getTestDataPath() + fileName);
            return mapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Failed to load JSON object '{}': {}", fileName, e.getMessage());
            throw new RuntimeException("Test data load failure: " + fileName, e);
        }
    }

    /**
     * Provides TestNG DataProvider compatible Object[][] from a JSON array.
     * Each array element becomes one test row.
     */
    public static Object[][] toDataProvider(String fileName, String... fields) {
        List<Map<String, Object>> data = loadJsonArray(fileName);
        Object[][] result = new Object[data.size()][fields.length];
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> row = data.get(i);
            for (int j = 0; j < fields.length; j++) {
                result[i][j] = row.get(fields[j]);
            }
        }
        return result;
    }

    // ── Dynamic Data Generators ───────────────────────────────────────────────

    public static String randomUserName()       { return faker.name().username(); }
    public static String randomSentence()       { return faker.lorem().sentence(); }
    public static String randomParagraph()      { return faker.lorem().paragraph(5); }
    public static String randomEmail()          { return faker.internet().emailAddress(); }
    public static String randomPhoneNumber()    { return faker.phoneNumber().phoneNumber(); }

    /** Generates a random string of given length. */
    public static String randomAlphanumeric(int length) {
        return faker.regexify("[a-zA-Z0-9]{" + length + "}");
    }

    /** Generates a very long string to test input limits. */
    public static String longText(int wordCount) {
        return faker.lorem().words(wordCount).toString();
    }

    // ── Security Payloads ─────────────────────────────────────────────────────

    public static String[] sqlInjectionPayloads() {
        return new String[]{
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "' UNION SELECT * FROM users --",
            "1; SELECT * FROM information_schema.tables",
            "admin'--",
            "' OR 1=1 --",
            "\" OR \"\"=\"",
            "'; EXEC xp_cmdshell('dir'); --"
        };
    }

    public static String[] xssPayloads() {
        return new String[]{
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "javascript:alert('XSS')",
            "<svg onload=alert(1)>",
            "'\"><script>alert(document.cookie)</script>",
            "<body onload=alert('XSS')>",
            "<!--<script>alert('XSS')</script>-->",
            "<iframe src='javascript:alert(1)'></iframe>"
        };
    }

    public static String[] negativeInputs() {
        return new String[]{
            "",                           // empty
            " ",                          // whitespace only
            null,                         // null
            "null",                       // string null
            "undefined",                  // JS undefined
            "!@#$%^&*()",                 // special chars
            "\n\n\n",                     // newlines
            "\t\t\t",                     // tabs
            "🤖🔥💣",                    // emojis
            randomAlphanumeric(5000),     // very long string
            "<>{}[]|\\^~`",              // more special chars
            "SELECT * FROM users",        // SQL without quotes
            "rm -rf /",                   // shell injection
            "../../../../etc/passwd"      // path traversal
        };
    }
}
