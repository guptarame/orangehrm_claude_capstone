package com.orangehrm.pages;

import com.orangehrm.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Parent class for all Page Objects.
 * <p>
 * Centralises the WebDriver reference and every explicit-wait helper so that
 * individual page classes only need to describe locators and business
 * actions, never raw Selenium wait boilerplate (keeps the POM classes thin
 * and consistent, per the Page Object Model pattern).
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        int explicitWaitSeconds = ConfigReader.getInstance().getExplicitWait();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Polls for at least {@code minCount} visible matches of {@code locator},
     * scoped to a caller-supplied timeout (mirrors {@link #isDisplayed(By, int)}
     * rather than the shared 15s {@link #wait} field).
     *
     * @return {@code true} if at least {@code minCount} matching elements are
     *         visible within the timeout, otherwise {@code false}
     */
    protected boolean areAtLeastNVisible(By locator, int minCount, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(d -> countVisible(d.findElements(locator)) >= minCount);
            return true;
        } catch (TimeoutException e) {
            // Fewer than minCount became visible within the window - a
            // first-class "not enough" outcome, not an error to hide.
            return false;
        }
    }

    private static long countVisible(List<WebElement> elements) {
        return elements.stream().filter(WebElement::isDisplayed).count();
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void waitForUrlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected void type(By locator, String text) {
        WebElement element = waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText();
    }

    protected boolean isDisplayed(By locator, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            // Element genuinely did not become visible within the window -
            // a first-class "not displayed" outcome, not an error to hide.
            return false;
        }
    }

    /**
     * Polls for the element's presence in the DOM (not visibility) with a fast
     * polling interval, so very short-lived elements can be observed.
     *
     * @return {@code true} if present within the timeout, otherwise {@code false}
     */
    protected boolean isPresent(By locator, Duration timeout, Duration pollingInterval) {
        try {
            new WebDriverWait(driver, timeout, pollingInterval)
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void navigateTo(String url) {
        driver.get(url);
    }
}
