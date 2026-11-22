# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

- **FR-001 (US-AUTH-001): Hardened login page controls verification**
  - Tightened footer social-link assertion from single-match to minimum-count check (≥2 visible links), closing a partial-regression blind spot where a collapsed footer would incorrectly pass as valid (TASK-012).
  - Added literal text assertions for the "Login" button and "Forgot your password?" link in the login page, enforcing that these controls are not just located structurally but also display the exact expected labels (TASK-013).
  - Split the NFR-002 HTTPS transport check out of the FR-001 scenario (`@TS_LOG_008`) into its own scenario (`@TS_LOG_013`), restoring one-scenario-per-requirement traceability and preventing an HTTPS regression from masking a FR-001 defect (TASK-014).

### Implementation Details

- **New Page Object Methods**
  - `BasePage.areAtLeastNVisible(By locator, int minCount, int timeoutSeconds)` — counts visible elements matching a locator with an explicit, per-call timeout (3 s for missing-controls group, unaffected by shared 15 s explicit-wait).
  - `LoginPage.getLoginButtonText()` — reads the visible text of the Login button, normalized for whitespace.
  - `LoginPage.getForgotPasswordLinkText()` — reads the visible text of the Forgot your password? link, normalized for whitespace.

- **New Test Scenario**
  - `@TS_LOG_013` ("Login page is served over HTTPS") — pre-submit transport security check under NFR-002, inheriting the same `Background` as FR-001's scenario so coverage is split rather than reduced.

- **Test Evidence**
  - `@TS_LOG_008` now spans 6 steps (up from 4) covering all 11 clauses of AC-001 (the 6 required controls plus 2 placeholder values plus 2 literal button/link labels plus minimum 2 footer social links), all passing.
  - `@TS_LOG_013` added as a 1-scenario verification of pre-submit HTTPS transport.
  - `ConfigReaderTest` (unit tests covering ConfigReader's security guards) remains at 7/7 passing.
  - Full suite: 13 scenarios, 46 steps, 12 passed, 1 pre-existing failure (loading indicator, unrelated to FR-001 and explicitly out-of-scope per requirements).

### Known Limitations

- **DR-022 (open):** No CI/CD workflow exists; this framework change is verified locally only. The PR carries no automated regression signal on push, and reports are retained locally in `target/cucumber-reports/` only. The framework is CI-ready (`headless=true` support, `-D` secret injection, JUnit-XML output); adding a workflow is a future, self-contained task outside this change's scope.
- **DR-024 (open):** NFR-002 (HTTPS transport) is cited throughout the code and framework but is not formally defined in `artifacts/requirements.md`. It is tracked only via the existing `# NFR-002` code-comment convention and cannot be verified against a requirements statement. Requires a user decision: either amend `requirements.md` with a measurable NFR-002 (preferred), or formally record it as inherited from prior unavailable work.
- **@TS_LOG_009 failure (pre-existing):** The "loading indicator displayed during authentication" scenario fails but is explicitly out of scope (part of AC2 — successful login positive flow — not FR-001). It is unrelated to this change, was reproduced on pre-task code, and is not fixed here.
- **DR-018 (resolved):** Prior work in this repository cited `AD-001–AD-009`, `DR-001–DR-017`, and `TASK-001–TASK-011` but their defining documents are unavailable. These IDs are recorded as `Not Found` and are permanently retired (never reused). New IDs start at `AD-010`, `DR-018`, and `TASK-012` respectively.

