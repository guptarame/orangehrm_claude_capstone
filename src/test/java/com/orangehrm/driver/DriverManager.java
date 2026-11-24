package com.orangehrm.driver;

import com.orangehrm.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Locale;

/**
 * TASK-005 (artifacts/impl-plan.md): per-thread {@link WebDriver} lifecycle.
 * AD-005 - holds the session in a {@code ThreadLocal<WebDriver>} so the
 * suite can run in parallel without scenarios sharing a browser session.
 * Applies the configured page-load timeout and deliberately applies no
 * implicit wait (AD-003) - synchronization is explicit-wait only, owned by
 * {@code pages.BasePage}.
 */
public final class DriverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    /**
     * Creates a new browser session for the calling thread using the
     * configured {@code browser} and {@code headless} values (AD-005). If
     * configuration or driver startup fails after the browser process has
     * already launched, that half-initialized session is quit immediately
     * so no orphan browser/driver process is left behind - it is never
     * registered in the {@code ThreadLocal}.
     */
    public static void createDriver() {
        ConfigReader config = ConfigReader.getInstance();
        String browser = config.getBrowser();
        boolean headless = config.isHeadless();
        WebDriver driver = launchBrowser(browser, headless);

        try {
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoadTimeoutSeconds()));
            // AD-003: deliberately no implicitlyWait(...) call anywhere in this class.
        } catch (RuntimeException configFailure) {
            driver.quit();
            throw configFailure;
        }

        LOGGER.info("Launched {} session (headless={})", browser, headless);
        DRIVER.set(driver);
    }

    private static WebDriver launchBrowser(String browser, boolean headless) {
        switch (browser.toLowerCase(Locale.ROOT)) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
                }
                return new ChromeDriver(chromeOptions);
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("-headless");
                }
                return new FirefoxDriver(firefoxOptions);
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
                }
                return new EdgeDriver(edgeOptions);
            default:
                throw new IllegalArgumentException(
                    "Unsupported browser '" + browser + "'; expected one of chrome | firefox | edge");
        }
    }

    /**
     * Returns the current thread's active session. Callers (pages, steps)
     * run only after {@code Hooks}' {@code @Before} has called
     * {@link #createDriver()}, so a missing session here is a lifecycle bug,
     * not a recoverable state - fails closed with a clear message rather
     * than silently creating a new session.
     */
    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No WebDriver session for this thread; createDriver() was not called");
        }
        return driver;
    }

    /**
     * Returns the current thread's session without creating one, or
     * {@code null} if none exists. Used for a best-effort failure screenshot
     * so a dead or absent session is never resurrected just to capture
     * diagnostics (AD-007).
     */
    public static WebDriver peekDriver() {
        return DRIVER.get();
    }

    /**
     * Quits the current thread's session, if any, and removes the
     * {@code ThreadLocal} entry so no stale reference survives into the next
     * scenario on a reused thread.
     */
    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
        }
        DRIVER.remove();
    }
}
