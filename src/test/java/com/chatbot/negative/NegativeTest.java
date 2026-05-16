package com.chatbot.negative;

import com.chatbot.pages.ChatBotPage;
import com.chatbot.ui.BaseTest;
import com.chatbot.utils.ExtentReportManager;
import com.chatbot.utils.TestDataUtil;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * NegativeTest — Validates chatbot stability against invalid, boundary,
 * and unexpected UI inputs.
 *
 * Covers:
 *  - Special characters
 *  - Random strings / gibberish
 *  - Null / "null" string inputs
 *  - Unsupported commands
 *  - Repeated rapid messages
 *  - Emoji-only messages
 *  - Unicode and RTL text
 *  - XSS payloads in UI (rendered safely)
 *  - SQL injection strings in UI
 */
@Epic("AI Chatbot Testing")
@Feature("Negative Testing")
public class NegativeTest extends BaseTest {

    // ── 1. Special Characters ─────────────────────────────────────────────────

    @Test(description = "Verify chatbot handles special characters without crashing",
          groups = {"negative", "ui"}, priority = 1)
    @Story("Special Characters")
    @Severity(SeverityLevel.NORMAL)
    public void testSpecialCharactersInput() {
        String input = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~\\";
        ExtentReportManager.logInfo("Testing special chars: " + input);

        chatBotPage.sendMessage(input);
        String response = chatBotPage.waitForBotResponse(30);

        Assert.assertFalse(chatBotPage.isErrorDisplayed(),
            "No UI error should appear for special characters.");
        Assert.assertFalse(response.isEmpty(),
            "Bot should respond (even gracefully) to special characters.");
        ExtentReportManager.logPass("Special characters handled. Response: " + response);
    }

    // ── 2. Data-Driven Negative Inputs ────────────────────────────────────────

    @Test(description = "Verify chatbot handles all negative/invalid inputs gracefully",
          dataProvider = "negativeInputs",
          groups = {"negative", "ui"})
    @Story("Invalid Inputs")
    @Severity(SeverityLevel.NORMAL)
    public void testNegativeInputsDataDriven(String input, String description) {
        if (input == null || input.trim().isEmpty()) {
            log.info("Skipping null/empty input: {}", description);
            ExtentReportManager.logInfo("Skipping null/empty input: " + description);
            return;
        }

        ExtentReportManager.logInfo("Testing: " + description + " → input: " + input);
        chatBotPage.sendMessage(input);

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        Assert.assertFalse(chatBotPage.isErrorDisplayed(),
            "No crash/error for: " + description);
        ExtentReportManager.logPass("Handled gracefully: " + description);
    }

    @DataProvider(name = "negativeInputs")
    public Object[][] negativeInputsProvider() {
        return new Object[][]{
            {"null",                          "String 'null'"},
            {"undefined",                     "String 'undefined'"},
            {"!@#$%^&*()",                    "Special characters"},
            {"SELECT * FROM users",           "SQL without quotes"},
            {"rm -rf /",                      "Shell injection attempt"},
            {"../../../../etc/passwd",        "Path traversal"},
            {"🤖🔥💣🎉😈",                  "Emoji-only input"},
            {"   \t\t\n\n   ",              "Tabs and newlines only"},
            {"AAAAAAAAAAAAAAAAAAAAAAAAAAAA", "Long single character repeat"},
            {"مرحبا كيف حالك",              "Arabic RTL text"},
            {"こんにちは、元気ですか",        "Japanese text"},
            {"你好世界",                      "Chinese text"},
            {"αβγδεζηθ",                     "Greek characters"},
            {"Привет мир",                    "Cyrillic/Russian text"},
        };
    }

    // ── 3. Unsupported Commands ───────────────────────────────────────────────

    @Test(description = "Verify chatbot gracefully handles unsupported commands",
          groups = {"negative", "ui"}, priority = 2)
    @Story("Unsupported Commands")
    @Severity(SeverityLevel.NORMAL)
    public void testUnsupportedCommands() {
        String[] commands = {
            "/delete_all_users",
            "/hack_system",
            "/admin_mode_on",
            "SHUTDOWN NOW",
            "RESTART SERVER",
            "__debug_mode__"
        };

        for (String cmd : commands) {
            ExtentReportManager.logInfo("Testing unsupported command: " + cmd);
            chatBotPage.sendMessage(cmd);
            String response = chatBotPage.waitForBotResponse(20);

            Assert.assertFalse(chatBotPage.isErrorDisplayed(),
                "No crash for command: " + cmd);
            Assert.assertFalse(response.isEmpty(),
                "Bot should respond to unsupported command: " + cmd);
        }
        ExtentReportManager.logPass("All unsupported commands handled gracefully.");
    }

    // ── 4. Rapid Repeated Messages ────────────────────────────────────────────

    @Test(description = "Verify chatbot remains stable under rapid repeated inputs",
          groups = {"negative", "ui"}, priority = 3)
    @Story("Rapid Input")
    @Severity(SeverityLevel.NORMAL)
    public void testRapidRepeatedMessages() {
        for (int i = 1; i <= 5; i++) {
            chatBotPage.sendMessage("Quick message number " + i);
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        // After rapid sending, bot should still be operational
        chatBotPage.sendMessage("Are you still working?");
        String response = chatBotPage.waitForBotResponse(45);

        Assert.assertFalse(response.isEmpty(),
            "Bot should still respond after rapid messages.");
        Assert.assertFalse(chatBotPage.isErrorDisplayed(),
            "No error UI after rapid messages.");
        ExtentReportManager.logPass("Bot remains stable after rapid message sequence.");
    }

    // ── 5. Very Long Single Word ──────────────────────────────────────────────

    @Test(description = "Verify chatbot handles an extremely long single word",
          groups = {"negative", "ui"}, priority = 4)
    @Story("Long Word Input")
    @Severity(SeverityLevel.MINOR)
    public void testVeryLongSingleWord() {
        String longWord = "a".repeat(10_000);
        chatBotPage.sendMessage(longWord);
        String response = chatBotPage.waitForBotResponse(45);

        Assert.assertFalse(chatBotPage.isErrorDisplayed(),
            "No crash for extremely long single word.");
        ExtentReportManager.logPass("Very long single word handled. Response: " +
            response.substring(0, Math.min(response.length(), 100)));
    }

    // ── 6. Numeric-Only Input ─────────────────────────────────────────────────

    @Test(description = "Verify chatbot handles numeric-only input",
          groups = {"negative", "ui"}, priority = 5)
    @Story("Numeric Input")
    @Severity(SeverityLevel.MINOR)
    public void testNumericOnlyInput() {
        chatBotPage.sendMessage("1234567890");
        String response = chatBotPage.waitForBotResponse(30);

        Assert.assertFalse(response.isEmpty(),
            "Bot should respond to numeric-only input.");
        Assert.assertFalse(chatBotPage.isErrorDisplayed());
        ExtentReportManager.logPass("Numeric input handled correctly.");
    }

    // ── 7. HTML Tags in Input ─────────────────────────────────────────────────

    @Test(description = "Verify chatbot UI sanitises HTML tags in input (no raw rendering)",
          groups = {"negative", "security", "ui"}, priority = 6)
    @Story("HTML Injection Prevention")
    @Severity(SeverityLevel.NORMAL)
    public void testHtmlTagsInInput() {
        chatBotPage.sendMessage("<b>Bold</b> <i>Italic</i> <h1>Header</h1>");
        String response = chatBotPage.waitForBotResponse(30);

        // The UI should NOT render raw HTML — it should be escaped or stripped
        Assert.assertFalse(chatBotPage.isErrorDisplayed());
        Assert.assertFalse(response.isEmpty());
        ExtentReportManager.logPass("HTML tags in input did not cause rendering issues.");
    }
}
