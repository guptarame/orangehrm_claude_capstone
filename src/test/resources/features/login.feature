Feature: OrangeHRM login page controls

  As a registered user of the OrangeHRM portal
  I want the login page to display every control required to attempt authentication
  So that I can trust the page is ready before any credential-handling behavior is tested

  Background:
    Given the user is on the OrangeHRM login page

  # FR-001 (AC-001): the login page displays all required controls once it finishes loading.
  @TS_LOG_001 @positive
  Scenario: All required login page controls are displayed
    Then all required login controls should be displayed
    And the Username field placeholder should read "Username"
    And the Password field placeholder should read "Password"
