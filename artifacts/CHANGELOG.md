# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Initial Maven + Selenium + Cucumber BDD framework** (FR-001, TASK-001–TASK-012)
  - Maven project bootstrap with Selenium 4.27.0, Cucumber-JVM 7.20.1, JUnit 4.13.2
  - Page Object Model implementation: `BasePage`, `LoginPage` with FR-001 control locators
  - Configuration layer: `ConfigReader` with `-D` → environment variable → properties file precedence, HTTPS fail-closed guard (AD-004)
  - WebDriver management: `DriverManager` with ThreadLocal per-scenario lifecycle (AD-005)
  - Hooks for per-scenario driver lifecycle and failure screenshot capture (AD-008)
  - Cucumber BDD feature file: `login.feature` with `@TS_LOG_001` scenario for AC-001 (FR-001)
  - Step definitions: `LoginSteps` binding Gherkin to `LoginPage` (zero Selenium calls; AD-002)
  - Multi-format reporting: HTML, JSON, JUnit-XML, and console output (AD-007)
  - Comprehensive unit test coverage: `ConfigReaderTest` for configuration precedence and HTTPS guard (AD-004)
  - Live-DOM selector spike (TASK-002): confirmed all 6 FR-001 controls and placeholder values against OrangeHRM demo

### Verification (FR-001 / AC-001)

- All 8 tests passing: 7 `ConfigReaderTest` unit tests + 1 `@TS_LOG_001` Cucumber scenario (4 steps)
- End-to-end validation: login page displays company logo, Username field (placeholder "Username"), Password field (placeholder "Password"), Login button, "Forgot your password?" link, and footer
- Traceability: FR-001 → AD-001–AD-007 → TASK-001–TASK-012 → test code with `// FR-001` and `@TS_LOG_001` tags

### Known Limitations

- **DR-005**: `artifacts/requirements.md` contains a stale line citation (claims LoginSteps.java FR-001 tags at lines 155/164/170, but actual file has them at 33/40/46). No impact on FR-001/AC-001 substance; flagged for future Step 1 revision.
- **DR-001**: NFR-002 (HTTPS transport) referenced as a project convention but has no formal definition in `artifacts/requirements.md`. HTTPS guard implemented and tested under AD-004; final NFR-002 decision escalated to user.
- **CI workflow**: Local-only verification (`mvn test`); no CI/GitHub Actions workflow included (DR-004 scope).
- **Out of scope**: Password masking, successful login flow, error flows, password recovery link (AC1.2, AC2, AC3, AC4 noted in `artifacts/requirements.md` Out of Scope).
