package com.chatbot.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * JavaScriptUtil — Helpers for executing JavaScript in the browser context.
 * Used for edge cases where Selenium's native methods are unreliable
 * (e.g. clicking hidden elements, scrolling, reading values).
 */
public class JavaScriptUtil {

    private static final Logger log = LogManager.getLogger(JavaScriptUtil.class);

    private JavaScriptUtil() {}

    private static JavascriptExecutor js(WebDriver driver) {
        return (JavascriptExecutor) driver;
    }

    // ── Click ─────────────────────────────────────────────────────────────────

    /** Click an element via JavaScript (bypasses visibility restrictions). */
    public static void click(WebDriver driver, WebElement element) {
        log.debug("JS click on element: {}", element);
        js(driver).executeScript("arguments[0].click();", element);
    }

    // ── Scroll ────────────────────────────────────────────────────────────────

    /** Scroll the element into the centre of the viewport. */
    public static void scrollIntoView(WebDriver driver, WebElement element) {
        js(driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    /** Scroll the page to the very bottom. */
    public static void scrollToBottom(WebDriver driver) {
        js(driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    /** Scroll the page to the very top. */
    public static void scrollToTop(WebDriver driver) {
        js(driver).executeScript("window.scrollTo(0, 0);");
    }

    /** Scroll a specific container element to its bottom (e.g. chat window). */
    public static void scrollElementToBottom(WebDriver driver, WebElement container) {
        js(driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", container);
    }

    // ── Value / Text ──────────────────────────────────────────────────────────

    /** Set an input field's value directly via JS (for React / Vue controlled inputs). */
    public static void setInputValue(WebDriver driver, WebElement element, String value) {
        js(driver).executeScript("arguments[0].value = arguments[1];", element, value);
        // Trigger React's onChange
        js(driver).executeScript(
            "var event = new Event('input', { bubbles: true });" +
            "arguments[0].dispatchEvent(event);", element);
        log.debug("JS setValue '{}' on element", value);
    }

    /** Clear an input by setting its value to empty string via JS. */
    public static void clearInput(WebDriver driver, WebElement element) {
        setInputValue(driver, element, "");
    }

    /** Read the innerText of an element via JS. */
    public static String getInnerText(WebDriver driver, WebElement element) {
        return (String) js(driver).executeScript("return arguments[0].innerText;", element);
    }

    /** Read an element's attribute value via JS. */
    public static String getAttribute(WebDriver driver, WebElement element, String attribute) {
        return (String) js(driver).executeScript(
            "return arguments[0].getAttribute(arguments[1]);", element, attribute);
    }

    // ── Page Info ─────────────────────────────────────────────────────────────

    /** Returns the current page title. */
    public static String getTitle(WebDriver driver) {
        return (String) js(driver).executeScript("return document.title;");
    }

    /** Returns the current page URL. */
    public static String getUrl(WebDriver driver) {
        return (String) js(driver).executeScript("return window.location.href;");
    }

    /** Returns true when the DOM is fully loaded (readyState == 'complete'). */
    public static boolean isDomReady(WebDriver driver) {
        return "complete".equals(
            js(driver).executeScript("return document.readyState;"));
    }

    /** Waits for DOM readyState to be 'complete', polling every 500ms up to 30s. */
    public static void waitForDomReady(WebDriver driver) {
        long timeout = System.currentTimeMillis() + 30_000;
        while (!isDomReady(driver) && System.currentTimeMillis() < timeout) {
            WaitUtil.sleep(500);
        }
        log.debug("DOM ready.");
    }

    // ── Highlight (debugging) ─────────────────────────────────────────────────

    /**
     * Briefly highlights an element with a red border (useful for debugging).
     * Restores the original border after 1 second.
     */
    public static void highlight(WebDriver driver, WebElement element) {
        String originalStyle = element.getAttribute("style");
        js(driver).executeScript(
            "arguments[0].setAttribute('style', arguments[1]);",
            element, "border: 3px solid red; background: yellow;");
        WaitUtil.sleep(800);
        js(driver).executeScript(
            "arguments[0].setAttribute('style', arguments[1]);",
            element, originalStyle != null ? originalStyle : "");
    }

    // ── Local Storage ─────────────────────────────────────────────────────────

    public static void setLocalStorage(WebDriver driver, String key, String value) {
        js(driver).executeScript(
            "window.localStorage.setItem(arguments[0], arguments[1]);", key, value);
    }

    public static String getLocalStorage(WebDriver driver, String key) {
        return (String) js(driver).executeScript(
            "return window.localStorage.getItem(arguments[0]);", key);
    }

    public static void clearLocalStorage(WebDriver driver) {
        js(driver).executeScript("window.localStorage.clear();");
    }
}
