# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Setup

Requires JDK 11+ and Maven. No virtual env / package manager step — Maven resolves
dependencies from `pom.xml` on first run.

```powershell
mvn clean install -DskipTests
```

### Run the tests

```bash
# Full suite (runs TestRunner.java, which drives all Cucumber features)
mvn test

# Single feature tag
mvn test -Dcucumber.filter.tags="@TS_LOG_001"

# Override browser / headless / target at runtime (see config.properties)
mvn test -Dbrowser=firefox -Dheadless=true
mvn test -Dvalid.username=... -Dvalid.password=...
```

Must be run from the repo root. `TestRunner.java` (JUnit + Cucumber runner) discovers
`src/test/resources/features/*.feature` and binds them to the step definitions in
`com.orangehrm.stepdefinitions`. No application server to start — tests drive the
public OrangeHRM demo site directly via Selenium WebDriver (`base.url` in
`config.properties`).

### Configuration

`src/test/resources/config.properties` holds `base.url`, `browser`, `headless`,
`explicit.wait`, `page.load.timeout`, `valid.username`, `valid.password`.
`ConfigReader` (`src/test/java/com/orangehrm/config/ConfigReader.java`) resolves
every key with precedence: `-D` system property → environment variable
(key upper-cased, `.` → `_`, e.g. `VALID_PASSWORD`) → the properties file.
`getBaseUrl()` fails closed unless the scheme is `https`, unless
`allow.insecure.base.url=true` is set for a local non-TLS sandbox (NFR-002).

### SDLC pipeline

```bash
# Start the full 8-step pipeline from Step 1
/orchestrator.prompt

# Resume at a specific step (e.g. after a pause)
/orchestrator.prompt resume step=<N>

# Check current pipeline state
/orchestrator.prompt status

# Restart the pipeline from scratch
/orchestrator.prompt restart
```

Each step produces a human review gate (Approve / Request Changes / Pause) before the next agent runs. State is tracked in `artifacts/ORCHESTRATION_LOG.md`.

---

## Application architecture

The repository is a **Selenium + Java + Cucumber BDD test automation framework**
(Page Object Model) targeting the OrangeHRM demo application's login module —
there is no application source under test in this repo; it drives the public
`opensource-demo.orangehrmlive.com` instance over HTTPS.

```
src/test/java/com/orangehrm/
├── config/ConfigReader.java      ← singleton config loader (-D / env / properties precedence, HTTPS guard)
├── driver/DriverManager.java     ← ThreadLocal WebDriver lifecycle (chrome | firefox | edge)
├── hooks/Hooks.java              ← Cucumber @Before/@After: driver setup, failure screenshots, teardown
├── pages/                        ← Page Object Model
│   ├── BasePage.java             ← shared explicit-wait helpers (no implicit wait is used)
│   ├── LoginPage.java
│   ├── DashboardPage.java
│   └── ResetPasswordPage.java
├── stepdefinitions/LoginSteps.java ← Given/When/Then bindings for login.feature
└── runner/TestRunner.java        ← JUnit + Cucumber entry point (@RunWith(Cucumber.class))

src/test/resources/
├── features/login.feature        ← Gherkin scenarios, tagged @TS_LOG_### / @positive / @negative / @validation
└── config.properties             ← environment configuration (see Configuration above)
```

### Key conventions

- **Page Object Model**: step definitions (`LoginSteps`) contain no Selenium calls
  directly — all locators and waits live in the `pages/` classes, which extend
  `BasePage` for shared explicit-wait helpers.
- **No implicit wait**: synchronization is explicit-wait only (`BasePage`), so
  "not displayed" assertions don't pay an implicit-wait timeout cost.
- **Thread-safe driver**: `DriverManager` uses a `ThreadLocal<WebDriver>` so the
  suite can run in parallel (Surefire forking / parallel Cucumber runners)
  without scenarios sharing a browser session.
- **Credential/HTTPS handling**: `ConfigReader.getBaseUrl()` is a preventive
  guard (fails closed unless `https`); `LoginSteps` also asserts the scheme
  post-submission as defence-in-depth (NFR-002). `getValidPassword()` refuses
  the committed demo password unless `base.url` targets the public demo host.
- **Failure diagnostics**: `Hooks.tearDown` attaches a screenshot to the
  Cucumber scenario on failure before quitting the driver.

### Which file to extend

- New scenarios for the login flow go in `src/test/resources/features/login.feature`,
  tagged `@TS_LOG_###` plus `@positive`/`@negative`/`@validation` as appropriate.
- New step bindings go in `LoginSteps.java`; new reusable page interactions go in
  the relevant `pages/*.java` class (never inline Selenium calls in step defs).
- `ConfigReaderTest.java` is the only unit-test class (Surefire `**/*Test.java`
  pattern) — it covers `ConfigReader`'s security guards, not general app logic.

---

## Agentic SDLC pipeline

### Pipeline steps and agents

Each agent delegates its full workflow to the corresponding skill — the agent file is a thin invoker, the skill is the source of truth.

| Step | Agent | Skill | Output artifact |
|------|-------|-------|-----------------|
| 1 | Requirements Agent | `write-requirements` | `artifacts/requirements.md` |
| 2 | Architect Agent | `design-architecture` | `artifacts/architecture.md` |
| 3 | Design Review Agent | `design-review` | `artifacts/design-review.md` |
| 4 | Planner Agent | `plan-implementation` | `artifacts/impl-plan.md` |
| 5 | Implementation Agent | `implement-task` | production code + tests |
| 6 | Review Agent | `code-review` | review findings + fixes |
| 7 | Verify Agent | `verify-implementation` | `artifacts/verification-report.md` |
| 8 | PR Agent | `create-pr` | pull request + `artifacts/CHANGELOG.md` |

All agents also use `sdlc-traceability` to maintain stable IDs across artifacts.

### Traceability ID conventions

| ID format | Scope |
|-----------|-------|
| `FR-###` | Functional requirement (in `requirements.md`) |
| `NFR-###` | Non-functional requirement (in `requirements.md`) |
| `AD-###` | Architecture decision (in `architecture.md`) |
| `DR-###` | Design-review finding (in `design-review.md`) |
| `TASK-###` | Implementation task (in `impl-plan.md`) |
| `@TS_LOG_###` | Gherkin scenario tag (in `login.feature`) |

IDs are assigned once and never renumbered. Downstream artifacts reference IDs instead of restating the content. Code and feature-file comments use these IDs to trace back to requirements (e.g. `// FR-001`, `# NFR-002`).

### Rules (path-scoped, auto-applied)

Rules in `.claude/rules/` load automatically when Claude works on matching files.
No explicit invocation needed — they're part of every relevant editing session.

| File | Paths matched | Governs |
|------|--------------|---------|
| `.claude/rules/code-quality.md` | `**/*.java`, `**/*.feature` | OWASP-safe, DRY, clear-code standards |
| `.claude/rules/tests.md` | `src/test/java/**/*.java`, `src/test/resources/features/**/*.feature` | coverage, structure, determinism |
| `.claude/rules/sdlc-artifacts.md` | `artifacts/**/*.md`, `artifacts/ORCHESTRATION_LOG.md` | document structure, traceability, writing quality |

### `.claude/` layout

```
.claude/
├── CLAUDE.md                          ← this file
├── settings.json                      ← permission configuration
├── agents/                            ← one agent per pipeline step
│   ├── requirements.agent.md
│   ├── architect.agent.md
│   ├── design-review.agent.md
│   ├── planner.agent.md
│   ├── implementation.agent.md
│   ├── review.agent.md
│   ├── verify.agent.md
│   └── pr.agent.md
├── skills/                            ← one skill per agent (workflow detail)
│   ├── read-user-story/
│   ├── sdlc-traceability/
│   ├── write-requirements/
│   ├── design-architecture/
│   ├── design-review/
│   ├── plan-implementation/
│   ├── implement-task/
│   ├── code-review/
│   ├── verify-implementation/
│   └── create-pr/
├── rules/                             ← path-scoped rules (auto-applied by Claude Code)
│   ├── code-quality.md
│   ├── tests.md
│   └── sdlc-artifacts.md
└── commands/
    └── orchestrator.prompt.md         ← pipeline entry point (invoked as /orchestrator.prompt)
```
