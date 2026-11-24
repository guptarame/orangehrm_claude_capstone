package com.orangehrm.stepdefinitions;

import com.orangehrm.config.ConfigReader;
import com.orangehrm.pages.LoginPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TASK-010 (artifacts/impl-plan.md): binds every step in
 * {@code @TS_LOG_001} to {@code pages.LoginPage} (AD-002). Contains
 * assertions and orchestration only - zero Selenium API calls and zero
 * locators; all DOM knowledge stays in {@code LoginPage}.
 *
 * <p>Note: Cucumber-java matches step text independently of the
 * {@code Given}/{@code When}/{@code Then} annotation used, so the
 * feature file's {@code And} steps are bound below with {@code @Then}.
 */
public class LoginSteps {

    private final LoginPage loginPage = new LoginPage();

    @Given("the user is on the OrangeHRM login page")
    public void theUserIsOnTheOrangeHrmLoginPage() {
        String baseUrl = ConfigReader.getInstance().getBaseUrl();
        loginPage.open(baseUrl);
    }

    // FR-001
    @Then("all required login controls should be displayed")
    public void allRequiredLoginControlsShouldBeDisplayed() {
        List<String> missingControls = loginPage.findMissingLoginControls();
        assertTrue("Missing login control(s): " + missingControls, missingControls.isEmpty());
    }

    // FR-001
    @Then("the Username field placeholder should read \"Username\"")
    public void theUsernameFieldPlaceholderShouldReadUsername() {
        assertEquals("Username", loginPage.getUsernamePlaceholder());
    }

    // FR-001
    @Then("the Password field placeholder should read \"Password\"")
    public void thePasswordFieldPlaceholderShouldReadPassword() {
        assertEquals("Password", loginPage.getPasswordPlaceholder());
    }
}
