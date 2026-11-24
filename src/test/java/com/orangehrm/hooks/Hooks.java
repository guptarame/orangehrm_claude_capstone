package com.orangehrm.hooks;

import com.orangehrm.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TASK-008 (artifacts/impl-plan.md): per-scenario driver lifecycle and
 * failure diagnostics (AD-005, AD-007). Hooks run per scenario, not per
 * suite, guaranteeing a fresh, isolated browser session for every scenario.
 */
public class Hooks {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hooks.class);

    /** Launches the driver before the first step of every scenario. */
    @Before
    public void setUp(Scenario scenario) {
        LOGGER.info("Starting scenario: {}", scenario.getName());
        DriverManager.createDriver();
    }

    /**
     * Attaches a PNG screenshot to the scenario only on failure, then always
     * quits the driver. Screenshot capture is best-effort - wrapped in its
     * own {@code try/catch} so a crashed session or a screenshot failure
     * never masks the original scenario failure, and {@link DriverManager#quitDriver()}
     * runs in a {@code finally} so the browser is never leaked.
     */
    @After
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                attachFailureScreenshot(scenario);
            }
        } finally {
            DriverManager.quitDriver();
            LOGGER.info("Finished scenario: {} status={}", scenario.getName(), scenario.getStatus());
        }
    }

    private void attachFailureScreenshot(Scenario scenario) {
        try {
            WebDriver driver = DriverManager.peekDriver();
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", scenario.getName());
            }
        } catch (RuntimeException screenshotFailure) {
            LOGGER.warn("Could not capture failure screenshot for scenario: {}", scenario.getName(), screenshotFailure);
        }
    }
}
