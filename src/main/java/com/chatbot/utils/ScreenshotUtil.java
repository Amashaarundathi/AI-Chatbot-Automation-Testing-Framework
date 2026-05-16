package com.chatbot.utils;

import com.chatbot.config.ConfigManager;
import io.qameta.allure.Allure;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ScreenshotUtil — Captures screenshots and attaches them to Allure / ExtentReports.
 */
public class ScreenshotUtil {

    private static final Logger log = LogManager.getLogger(ScreenshotUtil.class);
    private static final ConfigManager config = ConfigManager.getInstance();

    private ScreenshotUtil() {}

    /**
     * Captures a screenshot and saves it to disk.
     *
     * @param testName Name used in the file name.
     * @return Absolute path to the saved screenshot, or null on failure.
     */
    public static String captureScreenshot(String testName) {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            log.warn("Driver is null — cannot capture screenshot.");
            return null;
        }

        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName  = testName + "_" + timestamp + ".png";
            String dirPath   = config.getScreenshotPath();

            // Ensure directory exists
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            File srcFile  = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File(dirPath + fileName);
            FileUtils.copyFile(srcFile, destFile);

            log.info("Screenshot saved: {}", destFile.getAbsolutePath());

            // Attach to Allure
            attachToAllure(testName, srcFile);

            return destFile.getAbsolutePath();

        } catch (IOException e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Attaches a screenshot directly to the Allure report.
     */
    public static void attachToAllure(String name, File screenshotFile) {
        try {
            byte[] screenshotBytes = FileUtils.readFileToByteArray(screenshotFile);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshotBytes), ".png");
            log.debug("Screenshot attached to Allure report: {}", name);
        } catch (IOException e) {
            log.error("Failed to attach screenshot to Allure: {}", e.getMessage());
        }
    }

    /**
     * Captures a screenshot as a byte array (for inline use in reports).
     */
    public static byte[] captureAsBytes() {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) return new byte[0];
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            log.error("Failed to capture screenshot as bytes: {}", e.getMessage());
            return new byte[0];
        }
    }
}
