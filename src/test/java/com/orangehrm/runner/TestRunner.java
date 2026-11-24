package com.orangehrm.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * TASK-011 (artifacts/impl-plan.md): JUnit 4 + Cucumber entry point
 * (AD-001). Discovers {@code src/test/resources/features}, binds the
 * {@code stepdefinitions} and {@code hooks} glue packages, and emits
 * console plus HTML/JSON/JUnit-XML reports under {@code target/} - JSON and
 * XML are the machine-readable inputs the Verify Agent (Step 7, TASK-014)
 * consumes (AD-007). Run with {@code mvn test}; filter a single tag with
 * {@code -Dcucumber.filter.tags="@TS_LOG_001"}.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.orangehrm.stepdefinitions", "com.orangehrm.hooks"},
    plugin = {
        "pretty",
        "summary",
        "html:target/cucumber-reports/cucumber.html",
        "json:target/cucumber-reports/cucumber.json",
        "junit:target/cucumber-reports/cucumber.xml"
    },
    monochrome = true
)
public class TestRunner {
}
