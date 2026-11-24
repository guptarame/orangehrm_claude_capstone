package com.orangehrm.config;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * TASK-012 (artifacts/impl-plan.md): covers {@code ConfigReader}'s
 * security-relevant behaviour - {@code -D} -> env -> file precedence
 * (AD-004), {@link ConfigReader#getBaseUrl()}'s HTTPS fail-closed guard, its
 * documented insecure opt-out, and failing closed on a missing key or a
 * malformed URL. {@code ConfigReader} is a singleton over global system
 * properties, so every test below saves and restores any property it
 * mutates, keeping the suite order-independent and deterministic
 * (.claude/rules/tests.md). This test does not cite {@code NFR-002} - the
 * HTTPS guard traces only to the {@code AD-004} project convention (DR-001).
 */
public class ConfigReaderTest {

    // Backs the EnvironmentVariables @Rule's reflective env mutation; see
    // the pom.xml Surefire argLine comment this depends on (TASK-012).
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private final ConfigReader configReader = ConfigReader.getInstance();

    @After
    public void clearMutatedSystemProperties() {
        System.clearProperty("browser");
        System.clearProperty("explicit.wait");
        System.clearProperty("base.url");
        System.clearProperty("allow.insecure.base.url");
    }

    // AD-004: -D system property wins over config.properties.
    @Test
    public void getBrowser_systemPropertyOverridesFileValue() {
        System.setProperty("browser", "firefox");

        assertEquals("firefox", configReader.getBrowser());
    }

    // AD-004: an environment variable wins over config.properties when no -D is set.
    @Test
    public void getExplicitWaitSeconds_environmentVariableOverridesFileValue() {
        environmentVariables.set("EXPLICIT_WAIT", "77");

        assertEquals(77, configReader.getExplicitWaitSeconds());
    }

    // AD-004: -D system property wins over an environment variable too (full precedence chain).
    @Test
    public void getExplicitWaitSeconds_systemPropertyOverridesEnvironmentVariable() {
        environmentVariables.set("EXPLICIT_WAIT", "77");
        System.setProperty("explicit.wait", "55");

        assertEquals(55, configReader.getExplicitWaitSeconds());
    }

    // AD-004: getBaseUrl() fails closed on a non-https scheme.
    @Test
    public void getBaseUrl_rejectsNonHttpsUrl() {
        System.setProperty("base.url", "http://insecure.example.com/login");

        assertThrows(IllegalStateException.class, configReader::getBaseUrl);
    }

    // AD-004: the insecure opt-out is honoured only when explicitly set to "true".
    @Test
    public void getBaseUrl_allowsNonHttpsOnlyWhenOptOutExplicitlySet() {
        System.setProperty("base.url", "http://insecure.example.com/login");
        System.setProperty("allow.insecure.base.url", "true");

        assertEquals("http://insecure.example.com/login", configReader.getBaseUrl());
    }

    // AD-004: a malformed/scheme-less URL is a guard failure, not a silent pass-through.
    @Test
    public void getBaseUrl_failsClosedOnMalformedUrl() {
        System.setProperty("base.url", "not-a-valid-url");

        assertThrows(IllegalStateException.class, configReader::getBaseUrl);
    }

    // AD-004: a required key missing from -D, env, and the file fails closed rather than returning a default.
    @Test
    public void resolve_failsClosedOnMissingKey() {
        assertThrows(IllegalStateException.class, () -> configReader.resolve("totally.undefined.key.for.test"));
    }
}
