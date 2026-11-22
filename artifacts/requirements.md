# Requirements

## User Story Reference

**US-AUTH-001 — Login Functionality for OrangeHRM Portal**

As a registered user (Admin/Employee) of the OrangeHRM system, I want to securely log
into the application using my credentials, so that I can access my designated
dashboard and perform role-based HR management tasks.

Target URL: `https://opensource-demo.orangehrmlive.com/web/index.php/auth/login`

## Business Objective

Users must be able to confirm the login page itself is complete and renders every
control required to attempt authentication before any credential-handling behavior
(success, failure, or validation) can be meaningfully tested.

## Functional Requirement

### FR-001: Login page displays all required controls

**Statement:** When a user navigates to the OrangeHRM login page
(`.../auth/login`), the system shall display all of the following elements:
the company logo/branding banner; a Username input field with placeholder text
"Username"; a Password input field with placeholder text "Password"; a "Login"
submit button; a "Forgot your password?" link; and a footer containing the
OrangeHRM copyright notice and social links.

**Source:** Confluence page 13271042 ("Login Functionality for OrangeHRM Portal"), AC1.1
**Priority:** High

## Acceptance Criterion

### AC-001 (FR-001)

**Given** the user navigates to the OrangeHRM login page at
`https://opensource-demo.orangehrmlive.com/web/index.php/auth/login`
**When** the page finishes loading
**Then** the company logo/branding banner, the Username field (placeholder
"Username"), the Password field (placeholder "Password"), the "Login" button,
the "Forgot your password?" link, and the footer (OrangeHRM copyright and
social links) are all visible on the page

## Traceability

| ID | Source | Status |
| --- | --- | --- |
| FR-001 | Confluence page 13271042, AC1.1 | Approved |

## Out of Scope

- **AC1.2 — Password masking:** the password field masking entered characters
  (dots/asterisks) is a distinct, independently testable security/UI behavior,
  not part of the "controls are present" check in FR-001. (Already implemented
  and exercised by `TS_LOG_007`; not tagged `FR-001` in code, consistent with
  this document.)
- **AC2 — Successful login (positive flow):** authentication with valid
  Username/Password, redirect to `.../dashboard/index`, and the loading
  spinner/indicator shown during the authentication request.
- **AC3 — Unsuccessful login (negative flows):** the `"Invalid credentials"`
  error banner for bad username/password combinations, and the `"Required"`
  inline validation messages for empty Username/Password fields on submit.
- **AC4 — Password recovery link navigation:** clicking "Forgot your
  password?" navigating to `.../auth/requestPasswordResetCode`.
- **HTTPS/transport security:** the PRD does not state an explicit HTTPS
  requirement; the HTTPS-scheme assertions in the current code are tracked
  separately under `NFR-002` (non-functional) and are out of scope for this
  functional-requirements document.

## Open Questions

None. FR-001 (login-page-controls-display) was chosen as the single
independently-testable functional requirement under the "one behavior only"
rule, and this aligns with the existing `// FR-001` code trace tags in
`src/test/java/com/orangehrm/stepdefinitions/LoginSteps.java` (lines 155, 164,
170), which already mark the "all required login controls / placeholders
displayed" assertions as `FR-001`. No discrepancy with existing code tags was
found.
