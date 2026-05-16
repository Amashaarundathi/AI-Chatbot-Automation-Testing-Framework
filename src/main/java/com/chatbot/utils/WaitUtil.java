package com.chatbot.utils;

import com.chatbot.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.NoSuchElementException;

/**
 * WaitUtil — Reusable explicit and fluent wait strategies.
 */
public class WaitUtil {

    private static final Logger log = LogManager.getLogger(WaitUtil.class);
    private static final int TIMEOUT = ConfigManager.getInstance().getExplicitWait();

    private WaitUtil() {}

    private static WebDriverWait getWait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    private static WebDriverWait getWait(WebDriver driver, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    // ── Visibility ────────────────────────────────────────────────────────────

    public static WebElement waitForVisible(WebDriver driver, By locator) {
        log.debug("Waiting for element visible: {}", locator);
        return getWait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForVisible(WebDriver driver, By locator, int seconds) {
        return getWait(driver, seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForVisible(WebDriver driver, WebElement element) {
        return getWait(driver).until(ExpectedConditions.visibilityOf(element));
    }

    // ── Clickability ──────────────────────────────────────────────────────────

    public static WebElement waitForClickable(WebDriver driver, By locator) {
        log.debug("Waiting for element clickable: {}", locator);
        return getWait(driver).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static WebElement waitForClickable(WebDriver driver, WebElement element) {
        return getWait(driver).until(ExpectedConditions.elementToBeClickable(element));
    }

    // ── Text ──────────────────────────────────────────────────────────────────

    public static boolean waitForTextPresent(WebDriver driver, By locator, String text) {
        log.debug("Waiting for text '{}' in: {}", text, locator);
        return getWait(driver).until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public static boolean waitForTextNotEmpty(WebDriver driver, By locator) {
        return getWait(driver).until(d -> {
            WebElement el = d.findElement(locator);
            return el != null && !el.getText().trim().isEmpty();
        });
    }

    // ── Presence ──────────────────────────────────────────────────────────────

    public static WebElement waitForPresence(WebDriver driver, By locator) {
        return getWait(driver).until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    // ── Invisibility ──────────────────────────────────────────────────────────

    public static boolean waitForInvisible(WebDriver driver, By locator) {
        return getWait(driver).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ── Fluent Wait (polling) ─────────────────────────────────────────────────

    public static WebElement fluentWait(WebDriver driver, By locator, int timeoutSec, int pollMs) {
        FluentWait<WebDriver> wait = new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(timeoutSec))
            .pollingEvery(Duration.ofMillis(pollMs))
            .ignoring(NoSuchElementException.class);
        return wait.until(d -> d.findElement(locator));
    }

    // ── Hard sleep (use sparingly) ────────────────────────────────────────────

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
