package com.chatbot.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager — Singleton configuration loader.
 * Reads config.properties and exposes typed getters.
 */
public class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private final Properties props = new Properties();

    private ConfigManager() {
        loadProperties();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadProperties() {
        String configPath = "src/main/resources/config.properties";
        try (InputStream input = new FileInputStream(configPath)) {
            props.load(input);
            log.info("Configuration loaded from: {}", configPath);
        } catch (IOException e) {
            log.error("Failed to load config.properties: {}", e.getMessage());
            throw new RuntimeException("Cannot load configuration file.", e);
        }
    }

    // ── String Getters ───────────────────────────────────────────────────────
    public String get(String key) {
        String value = System.getProperty(key, props.getProperty(key));
        if (value == null) {
            log.warn("Config key not found: {}", key);
        }
        return value;
    }

    // ── Typed Getters ────────────────────────────────────────────────────────
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public long getLong(String key) {
        return Long.parseLong(get(key));
    }

    // ── Application ──────────────────────────────────────────────────────────
    public String getAppBaseUrl()         { return get("app.base.url"); }
    public String getApiBaseUrl()         { return get("api.base.url"); }
    public String getChatEndpoint()       { return get("api.chat.endpoint"); }
    public String getHealthEndpoint()     { return get("api.health.endpoint"); }
    public String getAuthEndpoint()       { return get("api.auth.endpoint"); }

    // ── Browser ──────────────────────────────────────────────────────────────
    public String getBrowser()            { return get("browser"); }
    public boolean isHeadless()           { return getBoolean("browser.headless"); }
    public boolean shouldMaximize()       { return getBoolean("browser.maximize"); }
    public int getImplicitWait()          { return getInt("implicit.wait"); }
    public int getExplicitWait()          { return getInt("explicit.wait"); }
    public int getPageLoadTimeout()       { return getInt("page.load.timeout"); }

    // ── API ───────────────────────────────────────────────────────────────────
    public int getApiTimeout()            { return getInt("api.timeout"); }
    public int getApiMaxRetries()         { return getInt("api.max.retries"); }
    public String getValidToken()         { return get("api.valid.token"); }
    public String getInvalidToken()       { return get("api.invalid.token"); }
    public String getExpiredToken()       { return get("api.expired.token"); }

    // ── Performance ──────────────────────────────────────────────────────────
    public int getPerfThreadCount()       { return getInt("perf.thread.count"); }
    public int getPerfRampUpSeconds()     { return getInt("perf.ramp.up.seconds"); }
    public int getPerfTestDuration()      { return getInt("perf.test.duration.seconds"); }
    public int getAcceptableResponseMs()  { return getInt("perf.acceptable.response.time.ms"); }
    public int getMaxErrorRatePercent()   { return getInt("perf.max.error.rate.percent"); }

    // ── Reporting & Logging ───────────────────────────────────────────────────
    public boolean takeScreenshotOnFail() { return getBoolean("screenshot.on.failure"); }
    public String getScreenshotPath()     { return get("screenshot.path"); }
    public String getReportOutputPath()   { return get("report.output.path"); }
    public String getExtentReportPath()   { return get("extent.report.path"); }
    public String getLogOutputPath()      { return get("log.output.path"); }
    public String getTestDataPath()       { return get("test.data.path"); }
    public String getEnvironment()        { return get("environment"); }
}
