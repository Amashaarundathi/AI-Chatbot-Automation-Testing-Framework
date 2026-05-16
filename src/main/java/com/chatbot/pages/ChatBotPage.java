package com.chatbot.pages;

import com.chatbot.utils.WaitUtil;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatBotPage — Page Object Model for the chatbot web interface.
 *
 * All UI element interactions go through this class.
 * Update the locators (By.*) to match your actual application's HTML.
 */
public class ChatBotPage {

    private static final Logger log = LogManager.getLogger(ChatBotPage.class);
    private final WebDriver driver;

    // ── Locators — update these to match your application ────────────────────
    private final By inputField       = By.cssSelector("textarea[data-testid='chat-input'], #chat-input, .chat-input");
    private final By sendButton       = By.cssSelector("button[data-testid='send-btn'], #send-button, .send-btn");
    private final By botMessages      = By.cssSelector(".bot-message, [data-role='assistant'], .message.bot");
    private final By userMessages     = By.cssSelector(".user-message, [data-role='user'], .message.user");
    private final By allMessages      = By.cssSelector(".message, .chat-message, .conversation-item");
    private final By typingIndicator  = By.cssSelector(".typing-indicator, .is-typing, [data-testid='typing']");
    private final By errorMessage     = By.cssSelector(".error-message, .chat-error, [data-testid='error']");
    private final By chatContainer    = By.cssSelector(".chat-container, #chat-window, .conversation");
    private final By clearChatButton  = By.cssSelector("[data-testid='clear-chat'], .clear-chat, #clear-btn");
    private final By loadingSpinner   = By.cssSelector(".loading, .spinner, [data-testid='loading']");
    // ─────────────────────────────────────────────────────────────────────────

    public ChatBotPage(WebDriver driver) {
        this.driver = driver;
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Step("Navigate to chatbot page: {url}")
    public ChatBotPage navigateTo(String url) {
        log.info("Navigating to: {}", url);
        driver.get(url);
        WaitUtil.waitForVisible(driver, chatContainer);
        return this;
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    @Step("Type message: {message}")
    public ChatBotPage typeMessage(String message) {
        log.info("Typing message: {}", message);
        WebElement input = WaitUtil.waitForClickable(driver, inputField);
        input.clear();
        input.sendKeys(message);
        return this;
    }

    @Step("Click Send button")
    public ChatBotPage clickSend() {
        log.info("Clicking send button");
        WaitUtil.waitForClickable(driver, sendButton).click();
        return this;
    }

    @Step("Send message via Enter key")
    public ChatBotPage sendViaEnterKey() {
        log.info("Sending via Enter key");
        WaitUtil.waitForClickable(driver, inputField).sendKeys(Keys.RETURN);
        return this;
    }

    @Step("Send message: {message}")
    public ChatBotPage sendMessage(String message) {
        typeMessage(message);
        clickSend();
        return this;
    }

    // ── Response Handling ─────────────────────────────────────────────────────

    @Step("Wait for bot response (max {maxWaitSeconds}s)")
    public String waitForBotResponse(int maxWaitSeconds) {
        log.info("Waiting for bot response...");

        // Wait for typing indicator to appear, then disappear
        try {
            WaitUtil.waitForVisible(driver, typingIndicator, 5);
            WaitUtil.waitForInvisible(driver, typingIndicator);
        } catch (Exception e) {
            log.debug("No typing indicator detected — waiting directly for message.");
        }

        // Wait for loading spinner
        try {
            WaitUtil.waitForInvisible(driver, loadingSpinner);
        } catch (Exception ignored) {}

        // Wait for a bot message to appear
        WaitUtil.waitForVisible(driver, botMessages, maxWaitSeconds);

        String response = getLatestBotResponse();
        log.info("Bot responded: {}", response);
        return response;
    }

    /** Returns the text of the most recent bot message. */
    public String getLatestBotResponse() {
        List<WebElement> messages = driver.findElements(botMessages);
        if (messages.isEmpty()) return "";
        return messages.get(messages.size() - 1).getText().trim();
    }

    /** Returns all bot message texts in order. */
    public List<String> getAllBotResponses() {
        return driver.findElements(botMessages).stream()
            .map(el -> el.getText().trim())
            .collect(Collectors.toList());
    }

    /** Returns all user message texts in order. */
    public List<String> getAllUserMessages() {
        return driver.findElements(userMessages).stream()
            .map(el -> el.getText().trim())
            .collect(Collectors.toList());
    }

    /** Returns count of all messages (user + bot) in the conversation. */
    public int getMessageCount() {
        return driver.findElements(allMessages).size();
    }

    // ── State Checks ──────────────────────────────────────────────────────────

    /** True if an error message is visible in the UI. */
    public boolean isErrorDisplayed() {
        try {
            return driver.findElement(errorMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** True if the typing indicator is currently visible. */
    public boolean isTyping() {
        try {
            return driver.findElement(typingIndicator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** True if the input field is enabled. */
    public boolean isInputEnabled() {
        try {
            return driver.findElement(inputField).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /** True if the send button is enabled. */
    public boolean isSendEnabled() {
        try {
            return driver.findElement(sendButton).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /** Returns text currently in the input field. */
    public String getInputFieldValue() {
        return driver.findElement(inputField).getAttribute("value");
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    @Step("Clear chat history")
    public ChatBotPage clearChat() {
        try {
            WaitUtil.waitForClickable(driver, clearChatButton).click();
            log.info("Chat cleared.");
        } catch (Exception e) {
            log.warn("Clear chat button not found: {}", e.getMessage());
        }
        return this;
    }

    /** Scrolls the chat window to the bottom. */
    public void scrollToBottom() {
        WebElement container = driver.findElement(chatContainer);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollTop = arguments[0].scrollHeight;", container);
    }

    /** Checks whether the bot's latest response contains a specific keyword. */
    public boolean responseContains(String keyword) {
        return getLatestBotResponse().toLowerCase().contains(keyword.toLowerCase());
    }

    /** Exposes the underlying WebDriver (e.g. for alert handling in security tests). */
    public WebDriver getDriver() { return driver; }

    /** Returns true if the chat container is visible on screen. */
    public boolean isChatVisible() {
        try {
            return driver.findElement(chatContainer).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
