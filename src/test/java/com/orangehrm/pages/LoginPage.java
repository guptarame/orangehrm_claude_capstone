package com.orangehrm.pages;

import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TASK-007 (artifacts/impl-plan.md): sole owner of all FR-001 DOM knowledge
 * for the OrangeHRM login page (AD-002). Reports state only - this class
 * contains no assertions; {@code stepdefinitions.LoginSteps} judges the
 * result (AD-006).
 *
 * <p>Locators below were confirmed against the live page at the AC-001 URL
 * by the TASK-002 spike (not guessed): inspection date 2026-11-24, Chrome
 * 158 (headless, via Selenium ChromeDriver 158.0.8127.88), observed site
 * build "OrangeHRM OS 5.9". Each locator resolved to exactly one visible
 * element at inspection time; see the TASK-002 commit message for the full
 * inventory. The "Forgot your password?" control renders as a
 * {@code <p>} (not an {@code <a>}) inside {@code div.orangehrm-login-forgot}
 * - FR-001 only requires it to be visible, not clicked (AC4 navigation is
 * out of scope).
 */
public class LoginPage extends BasePage {

    // FR-001 - confirmed by TASK-002; see class-level inspection note above.
    private static final By LOGO = By.className("orangehrm-login-branding");
    private static final By USERNAME_INPUT = By.name("username");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By FORGOT_PASSWORD_LINK = By.className("orangehrm-login-forgot-header");
    private static final By FOOTER = By.className("orangehrm-login-footer");

    /**
     * Short per-control visibility wait used only by
     * {@link #findMissingLoginControls()}. Deliberately shorter than the
     * configured default {@code explicit.wait}: {@link #open(String)} has
     * already waited for the Username field, establishing AC-001's "page
     * finished loading" precondition, so every other control should already
     * be visible by the time this check runs - a short wait still gives a
     * genuinely missing control room to be detected without inflating a
     * worst-case (all-missing) run to the sum of six full default timeouts.
     */
    private static final int SHORT_WAIT_SECONDS = 3;

    // FR-001 - human-readable control names, in the order AC-001 states them.
    private Map<String, By> requiredControls() {
        Map<String, By> controls = new LinkedHashMap<>();
        controls.put("company logo/branding banner", LOGO);
        controls.put("Username field", USERNAME_INPUT);
        controls.put("Password field", PASSWORD_INPUT);
        controls.put("Login button", LOGIN_BUTTON);
        controls.put("Forgot your password? link", FORGOT_PASSWORD_LINK);
        controls.put("footer (copyright and social links)", FOOTER);
        return controls;
    }

    /**
     * Navigates to {@code baseUrl} and waits for the Username field,
     * establishing AC-001's "page finishes loading" precondition before any
     * control check runs (AD-003).
     */
    public void open(String baseUrl) {
        navigateTo(baseUrl);
        waitForVisible(USERNAME_INPUT);
    }

    /**
     * FR-001: evaluates all six required controls independently - one
     * visibility check per control, each with its own short wait, with no
     * short-circuiting - and returns the human-readable names of the
     * controls that are not visible (AD-006). An empty list means AC-001's
     * "all controls visible" outcome holds.
     */
    public List<String> findMissingLoginControls() {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, By> control : requiredControls().entrySet()) {
            if (!isVisible(control.getValue(), SHORT_WAIT_SECONDS)) {
                missing.add(control.getKey());
            }
        }
        return missing;
    }

    /** FR-001: the Username field's {@code placeholder} attribute value, for the step layer to compare. */
    public String getUsernamePlaceholder() {
        return readAttribute(USERNAME_INPUT, "placeholder");
    }

    /** FR-001: the Password field's {@code placeholder} attribute value, for the step layer to compare. */
    public String getPasswordPlaceholder() {
        return readAttribute(PASSWORD_INPUT, "placeholder");
    }
}
