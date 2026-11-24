package com.orangehrm.pages;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * TASK-006 (artifacts/impl-plan.md): shared explicit-wait helpers reused by
 * every page object (AD-003). Synchronization is explicit-wait only - no
 * implicit wait is configured anywhere in this class or its callers, so a
 * legitimate "not displayed" outcome costs exactly its own timeout instead
 * of a hidden global one.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    private final ConfigReader config;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
        this.config = ConfigReader.getInstance();
    }

    /** Navigates the current session to {@code url}. No wait is performed here. */
    protected void navigateTo(String url) {
        driver.get(url);
    }

    /** Waits up to the configured default {@code explicit.wait} for {@code locator} to become visible. */
    protected WebElement waitForVisible(By locator) {
        return waitForVisible(locator, config.getExplicitWaitSeconds());
    }

    /** Waits up to {@code timeoutSeconds} (a caller-supplied override, e.g. a short per-control check) for {@code locator} to become visible. */
    protected WebElement waitForVisible(By locator, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Visibility predicate used by every FR-001 control check (AD-006):
     * catches {@link TimeoutException} and returns {@code false} so "not
     * displayed" is a reportable, first-class outcome rather than a thrown
     * error that would short-circuit the remaining controls.
     */
    protected boolean isVisible(By locator, int timeoutSeconds) {
        try {
            waitForVisible(locator, timeoutSeconds);
            return true;
        } catch (TimeoutException notVisible) {
            return false;
        }
    }

    /** Convenience overload of {@link #isVisible(By, int)} using the configured default timeout. */
    protected boolean isVisible(By locator) {
        return isVisible(locator, config.getExplicitWaitSeconds());
    }

    /** Waits for {@code locator} to be visible, then reads its {@code attributeName} HTML attribute (not a live DOM property). */
    protected String readAttribute(By locator, String attributeName) {
        return waitForVisible(locator).getDomAttribute(attributeName);
    }
}
