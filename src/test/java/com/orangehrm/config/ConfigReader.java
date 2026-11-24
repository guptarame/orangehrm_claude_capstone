package com.orangehrm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * TASK-004 (artifacts/impl-plan.md): thread-safe singleton configuration
 * resolver. AD-004 - resolves every key with {@code -D} system property ->
 * environment variable (key upper-cased, {@code "."} -> {@code "_"}) ->
 * {@code config.properties} precedence. {@link #getBaseUrl()} fails closed
 * unless the resolved scheme is {@code https}, with a documented, logged
 * opt-out ({@code allow.insecure.base.url=true}) for a local, non-TLS
 * sandbox only.
 *
 * <p>This guard is planned solely as the AD-004 project convention; it is
 * not traced to {@code NFR-002}, which has no statement or acceptance
 * criterion in any approved artifact (DR-001).
 */
public final class ConfigReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";

    /** Initialization-on-demand holder: thread-safe singleton with no explicit lock. */
    private static final class Holder {
        private static final ConfigReader INSTANCE = new ConfigReader();
    }

    private final Properties fileProperties;

    private ConfigReader() {
        this.fileProperties = loadProperties();
    }

    public static ConfigReader getInstance() {
        return Holder.INSTANCE;
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + CONFIG_FILE + " on the test classpath");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
        LOGGER.info("Loaded {} with {} keys", CONFIG_FILE, properties.size());
        return properties;
    }

    /**
     * Resolves {@code key} with {@code -D} system property -> environment
     * variable (key upper-cased, {@code "."} -> {@code "_"}) ->
     * {@code config.properties} precedence. Fails closed (throws) when none
     * of the three sources defines the key, rather than silently falling
     * back to an invented default.
     */
    String resolve(String key) {
        String fromSystemProperty = System.getProperty(key);
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return fromSystemProperty;
        }

        String envKey = key.toUpperCase().replace('.', '_');
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }

        String fromFile = fileProperties.getProperty(key);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile;
        }

        throw new IllegalStateException("Required configuration key '" + key
            + "' is not set via -D" + key + ", environment variable " + envKey
            + ", or " + CONFIG_FILE);
    }

    private String resolveOrDefault(String key, String defaultValue) {
        try {
            return resolve(key);
        } catch (IllegalStateException notSet) {
            return defaultValue;
        }
    }

    /**
     * FR-001: supplies the AC-001 target URL. Fails closed unless the
     * resolved scheme is {@code https}, unless {@code allow.insecure.base.url=true}
     * is explicitly set for a local, non-TLS sandbox (AD-004). Treats a
     * missing key or a malformed/scheme-less URL as a guard failure.
     */
    public String getBaseUrl() {
        String rawUrl = resolve("base.url");
        URI uri;
        try {
            uri = new URI(rawUrl);
        } catch (URISyntaxException malformed) {
            throw new IllegalStateException("base.url '" + rawUrl + "' is malformed or scheme-less", malformed);
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalStateException("base.url '" + rawUrl + "' is scheme-less");
        }

        boolean allowInsecure = Boolean.parseBoolean(resolveOrDefault("allow.insecure.base.url", "false"));
        if (!"https".equalsIgnoreCase(scheme)) {
            if (allowInsecure) {
                LOGGER.warn("base.url does not use https; proceeding only because "
                    + "allow.insecure.base.url=true (local sandbox opt-out)");
            } else {
                throw new IllegalStateException(
                    "base.url must use https (got scheme '" + scheme
                        + "'); set allow.insecure.base.url=true only for a local, non-TLS sandbox");
            }
        }
        return rawUrl;
    }

    /** DriverManager (AD-005): {@code chrome | firefox | edge}. */
    public String getBrowser() {
        return resolve("browser");
    }

    /** DriverManager (AD-005): whether to launch the browser headless. */
    public boolean isHeadless() {
        return Boolean.parseBoolean(resolve("headless"));
    }

    /** Seconds. BasePage's (AD-003) default per-control explicit-wait timeout. */
    public int getExplicitWaitSeconds() {
        return Integer.parseInt(resolve("explicit.wait"));
    }

    /** Seconds. DriverManager's (AD-005) WebDriver page-load timeout. */
    public int getPageLoadTimeoutSeconds() {
        return Integer.parseInt(resolve("page.load.timeout"));
    }
}
