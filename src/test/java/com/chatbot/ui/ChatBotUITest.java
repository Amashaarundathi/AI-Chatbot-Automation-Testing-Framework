package com.chatbot.ui;

import com.chatbot.utils.ExtentReportManager;
import com.chatbot.utils.TestDataUtil;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * ChatBotUITest — UI automation tests for the chatbot web interface.
 *
 * Covers:
 *  - Page load and element visibility
 *  - Greeting and basic conversation flow
 *  - Send via button and Enter key
 *  - Input field behaviour
 *  - Valid conversation scenarios (data-driven)
 *  - Empty / whitespace input handling
 *  - Long-text input handling
 *  - Multiple sequential messages
 *  - Conversation history persistence
 */
@Epic("AI Chatbot Testing")
@Feature("UI Automation")
public class ChatBotUITest extends BaseTest {

    // ── 1. Page Load ──────────────────────────────────────────────────────────

    @Test(description = "Verify chatbot page loads and UI elements are visible",
          groups = {"smoke", "ui"})
    @Story("Page Load")
    @Severity(SeverityLevel.BLOCKER)
    public void testPageLoadsSuccessfully() {
        ExtentReportManager.logInfo("Verifying chatbot UI elements are visible.");

        Assert.assertTrue(chatBotPage.isChatVisible(),
            "Chat container should be visible after page load.");
        Assert.assertTrue(chatBotPage.isInputEnabled(),
            "Input field should be enabled.");
        Assert.assertTrue(chatBotPage.isSendEnabled(),
            "Send button should be enabled.");

        ExtentReportManager.logPass("Page loaded with all UI elements visible.");
    }

    // ── 2. Greeting ───────────────────────────────────────────────────────────

    @Test(description = "Verify chatbot responds to a greeting message",
          groups = {"smoke", "functional", "ui"})
    @Story("Greeting Test")
    @Severity(SeverityLevel.CRITICAL)
    public void testGreetingResponse() {
        ExtentReportManager.logInfo("Sending greeting: 'Hello'");

        chatBotPage.sendMessage("Hello");
        String response = chatBotPage.waitForBotResponse(30);

        Assert.assertFalse(response.isEmpty(),
            "Bot should respond to a greeting.");
        Assert.assertTrue(
            response.toLowerCase().matches(".*(hello|hi|hey|greet|welcome|assist|help).*"),
            "Bot response should contain a greeting keyword. Got: " + response
        );

        ExtentReportManager.logPass("Greeting response validated: " + response);
    }

    // ── 3. Send Button ────────────────────────────────────────────────────────

    @Test(description = "Verify message can be sent using the Send button",
          groups = {"smoke", "ui"})
    @Story("Send Interaction")
    @Severity(SeverityLevel.CRITICAL)
    public void testSendViaButton() {
        chatBotPage.typeMessage("What can you do?");
        chatBotPage.clickSend();
        String response = chatBotPage.waitForBotResponse(30);

        Assert.assertFalse(response.isEmpty(), "Bot should respond after clicking Send.");
        ExtentReportManager.logPass("Send button works correctly.");
    }

    // ── 4. Enter Key ──────────────────────────────────────────────────────────

    @Test(description = "Verify message can be sent using the Enter key",
          groups = {"ui"})
    @Story("Send Interaction")
    @Severity(SeverityLevel.NORMAL)
    public void testSendViaEnterKey() {
        chatBotPage.typeMessage("Tell me a joke");
        chatBotPage.sendViaEnterKey();
        String response = chatBotPage.waitForBotResponse(30);

        Assert.assertFalse(response.isEmpty(), "Bot should respond when Enter key is used.");
        ExtentReportManager.logPass("Enter key send works correctly.");
    }

    // ── 5. Input Cleared After Send ───────────────────────────────────────────

    @Test(description = "Verify input field is cleared after a message is sent",
          groups = {"ui"})
    @Story("Input Behaviour")
    @Severity(SeverityLevel.NORMAL)
    public void testInputClearedAfterSend() {
        chatBotPage.sendMessage("Testing input clear");

        String inputValue = chatBotPage.getInputFieldValue();
        Assert.assertTrue(inputValue == null || inputValue.isEmpty(),
            "Input field should be empty after sending a message.");
        ExtentReportManager.logPass("Input field cleared after send.");
    }

    // ── 6. Data-Driven Valid Prompts ──────────────────────────────────────────

    @Test(description = "Validate bot responds to various valid user prompts",
          dataProvider = "validPrompts",
          groups = {"functional", "ui"})
    @Story("Valid Prompt Responses")
    @Severity(SeverityLevel.NORMAL)
    public void testValidPromptsDataDriven(String prompt, String expectedKeyword) {
        ExtentReportManager.logInfo("Testing prompt: " + prompt);

        chatBotPage.sendMessage(prompt);
        String response = chatBotPage.waitForBotResponse(30);

        Assert.assertFalse(response.isEmpty(),
            "Bot should respond to: " + prompt);

        if (expectedKeyword != null && !expectedKeyword.isEmpty()) {
            Assert.assertTrue(
                response.toLowerCase().contains(expectedKeyword.toLowerCase()),
                String.format("Response to '%s' should contain '%s'. Got: %s",
                    prompt, expectedKeyword, response)
            );
        }

        ExtentReportManager.logPass(
            String.format("Prompt '%s' → response contains '%s'", prompt, expectedKeyword));
    }

    @DataProvider(name = "validPrompts", parallel = true)
    public Object[][] validPromptsProvider() {
        return new Object[][]{
            {"Hello",                     "hello"},
            {"What is your name?",        ""},
            {"How can you help me?",      "help"},
            {"Tell me about your features", ""},
            {"What are your capabilities?", ""},
            {"I need support",            ""},
            {"Thank you",                 ""},
            {"Goodbye",                   ""},
        };
    }

    // ── 7. Empty Input ────────────────────────────────────────────────────────

    @Test(description = "Verify empty input does not send a message",
          groups = {"negative", "ui"})
    @Story("Empty Input Handling")
    @Severity(SeverityLevel.NORMAL)
    public void testEmptyInputNotSent() {
        int messagesBefore = chatBotPage.getMessageCount();

        chatBotPage.typeMessage("");
        chatBotPage.clickSend();

        // Give time to detect any unexpected message
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        int messagesAfter = chatBotPage.getMessageCount();
        Assert.assertEquals(messagesAfter, messagesBefore,
            "Message count should not increase for empty input.");
        ExtentReportManager.logPass("Empty input correctly rejected.");
    }

    // ── 8. Whitespace-Only Input ──────────────────────────────────────────────

    @Test(description = "Verify whitespace-only input is handled gracefully",
          groups = {"negative", "ui"})
    @Story("Whitespace Input")
    @Severity(SeverityLevel.MINOR)
    public void testWhitespaceOnlyInput() {
        chatBotPage.typeMessage("     ");
        chatBotPage.clickSend();

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Either the message wasn't sent, or the bot returns a graceful response
        // In both cases, no crash should occur
        Assert.assertFalse(chatBotPage.isErrorDisplayed(),
            "No error UI should be shown for whitespace input.");
        ExtentReportManager.logPass("Whitespace input handled without crash.");
    }

    // ── 9. Long Text Input ────────────────────────────────────────────────────

    @Test(description = "Verify chatbot handles very long text input gracefully",
          groups = {"negative", "ui"})
    @Story("Long Input")
    @Severity(SeverityLevel.NORMAL)
    public void testLongTextInput() {
        String longText = TestDataUtil.longText(200);
        ExtentReportManager.logInfo("Sending long text (" + longText.length() + " chars)");

        chatBotPage.sendMessage(longText);
        String response = chatBotPage.waitForBotResponse(45);

        Assert.assertFalse(response.isEmpty(),
            "Bot should respond to long text input.");
        Assert.assertFalse(chatBotPage.isErrorDisplayed(),
            "No error UI should appear for long input.");
        ExtentReportManager.logPass("Long text input handled gracefully.");
    }

    // ── 10. Multiple Sequential Messages ─────────────────────────────────────

    @Test(description = "Verify chatbot handles multiple sequential messages in a conversation",
          groups = {"functional", "ui"})
    @Story("Multi-turn Conversation")
    @Severity(SeverityLevel.NORMAL)
    public void testMultipleSequentialMessages() {
        String[] messages = {
            "Hello",
            "What is your name?",
            "How can you help me?",
            "Thank you, goodbye!"
        };

        for (String msg : messages) {
            ExtentReportManager.logInfo("Sending: " + msg);
            chatBotPage.sendMessage(msg);
            String response = chatBotPage.waitForBotResponse(30);
            Assert.assertFalse(response.isEmpty(),
                "Bot should respond to: " + msg);
        }

        int totalBotMessages = chatBotPage.getAllBotResponses().size();
        Assert.assertEquals(totalBotMessages, messages.length,
            "Bot should have " + messages.length + " responses.");

        ExtentReportManager.logPass("Multi-turn conversation successful with " + messages.length + " exchanges.");
    }

    // ── 11. Conversation History ──────────────────────────────────────────────

    @Test(description = "Verify conversation history persists across multiple messages",
          groups = {"functional", "ui"})
    @Story("Conversation History")
    @Severity(SeverityLevel.NORMAL)
    public void testConversationHistoryPersists() {
        chatBotPage.sendMessage("Remember: my name is TestUser");
        chatBotPage.waitForBotResponse(30);

        chatBotPage.sendMessage("What is my name?");
        String response = chatBotPage.waitForBotResponse(30);

        Assert.assertFalse(chatBotPage.getAllBotResponses().isEmpty(),
            "Conversation history should be visible.");
        Assert.assertTrue(chatBotPage.getAllUserMessages().size() >= 2,
            "Both user messages should be in the chat history.");

        ExtentReportManager.logPass("Conversation history persists correctly.");
    }
}
