# Implementation Plan

## Overview

This plan is a **full regeneration, not an amendment**. Per design-review finding
`DR-002` (High, resolved in-review), the previous `artifacts/impl-plan.md` was
written against a superseded architecture baseline (commit `b448cb4`) that assumed
a fully implemented Maven/Selenium project. That assumption is false on this
branch: `test/pipeline-rerun` has no `pom.xml` and no `src/` tree at all
(re-confirmed for this step via `git ls-files src pom.xml`, which returns nothing).
The superseded `TASK-012`–`TASK-018` content is therefore discarded, not
reconciled, and task numbering **restarts at `TASK-001`** — no live code citation
exists on this branch to collide with.

Scope is exactly one functional requirement, `FR-001` (`AC-001`): the OrangeHRM
login page displays its six required controls, with the Username and Password
placeholder values. The work is **greenfield construction** of the four-layer test
framework proposed in `artifacts/architecture.md` (`AD-001`–`AD-007`), from an
empty repository to one passing scenario, `@TS_LOG_001`.

Five delivery threads:

1. **Epic A — Bootstrap (`TASK-001`).** There is no build at all yet, so the
   Maven project, dependency set, and package skeleton are the first deliverable.
   Nothing else can compile until this lands.
2. **Epic B — De-risk the unknown (`TASK-002`).** `DR-003` records that the live
   DOM for the six `FR-001` controls was never inspected. This plan makes that
   inspection an explicit, early, independently-verifiable task whose output
   (a confirmed selector inventory) is an input to `LoginPage` — rather than a
   blind guess baked into a later coding task. It has no prerequisite and runs
   concurrently with `TASK-001`.
3. **Epic C — Infrastructure layer (`TASK-003`–`TASK-005`, `TASK-012`).**
   `config.properties`, `ConfigReader` (precedence + HTTPS fail-closed guard),
   `DriverManager`, and the `ConfigReader` unit tests. Foundational, so all are
   High priority and precede the page and glue layers.
4. **Epic D — Page Object and glue layers (`TASK-006`–`TASK-011`).** `BasePage`,
   `LoginPage` (sole owner of `FR-001` locators), `Hooks`, `login.feature`
   `@TS_LOG_001`, `LoginSteps`, and `TestRunner` with reporting. This is where
   `FR-001`'s verification actually becomes executable.
5. **Epic E — Pipeline gates (`TASK-013`–`TASK-015`).** Code review (Step 6),
   verification run (Step 7), pull request and changelog (Step 8).

Delivery sequence: `TASK-001` ∥ `TASK-002` → `TASK-003` → `TASK-004` →
(`TASK-005` ∥ `TASK-006` ∥ `TASK-012`) → (`TASK-007` ∥ `TASK-008` ∥ `TASK-009`)
→ `TASK-010` → `TASK-011` → `TASK-013` → `TASK-014` → `TASK-015`.

**Requirement coverage check.** Every approved requirement maps to at least one
task, and every task traces to an approved requirement or an architecture
decision:

| Requirement / decision | Tasks |
| --- | --- |
| `FR-001` / `AC-001` (the only approved requirement) | `TASK-002`, `TASK-007`, `TASK-009`, `TASK-010`, `TASK-014` (directly); `TASK-001`, `TASK-003`–`TASK-006`, `TASK-008`, `TASK-011` (enabling) |
| `AD-001` build/stack | `TASK-001`, `TASK-009`, `TASK-011` |
| `AD-002` Page Object Model | `TASK-007`, `TASK-010` |
| `AD-003` explicit waits only | `TASK-006`, `TASK-007` |
| `AD-004` configuration + HTTPS guard | `TASK-003`, `TASK-004`, `TASK-012` |
| `AD-005` driver lifecycle | `TASK-005`, `TASK-008` |
| `AD-006` failure reporting strategy | `TASK-007`, `TASK-009`, `TASK-010` |
| `AD-007` reporting and diagnostics | `TASK-008`, `TASK-011`, `TASK-014` |
| `NFR-001`, `NFR-002` | **No task.** Neither is defined in `artifacts/requirements.md` (`DR-001`). The HTTPS guard is planned under `AD-004` only — see [Risks and Open Questions](#risks-and-open-questions). |

## Source References

- Architecture: `artifacts/architecture.md` (commit `3749dea`, human-approved;
  `AD-001`–`AD-007`)
- Requirements: `artifacts/requirements.md` (commit `3acb3f5`, human-approved;
  `FR-001`, `AC-001`)
- Design review: `artifacts/design-review.md` (`DR-001`–`DR-009`, human-approved;
  hand-off instructions in § Readiness Recommendation)

## Assumptions and Constraints

- **Confirmed:** no `pom.xml` and no `src/` tree exist on this branch
  (`git ls-files src pom.xml` returns nothing). All code in this plan is new.
  `.gitignore` already excludes `target/`, so no build-output task is needed.
- **Confirmed constraint:** all code lives under `src/test/java` /
  `src/test/resources` — this is a test framework, not a deployable application
  (`.claude/CLAUDE.md`; `artifacts/architecture.md` § Assumptions).
- **Confirmed constraint:** step definitions contain zero direct Selenium calls;
  locators and waits live only in `pages/*` (`AD-002`).
- **Confirmed constraint:** no implicit wait anywhere; explicit waits only
  (`AD-003`).
- **Confirmed constraint:** `FR-001` is verified by exactly one scenario,
  `@TS_LOG_001`, whose step methods carry `// FR-001` trace comments.
- **Constraint from `DR-003`:** no task in this plan states a selector string.
  `TASK-002` produces the confirmed selectors; `TASK-007` consumes them. No later
  task may assume a selector that `TASK-002` has not verified against the live
  page.
- **Constraint from `DR-006`:** no task is planned for `DashboardPage` or
  `ResetPasswordPage` — they are out-of-scope placeholders for future stories
  (AC2, AC4).
- **Constraint from `DR-004`:** no CI-workflow task is planned; local, on-demand
  verification is the accepted operating model for this run.
- **Constraint from `DR-001`:** no `NFR-002` task is planned, because no
  `NFR-002` is defined in any approved artifact. HTTPS-related work traces to
  `AD-004` instead.
- **Assumption:** the external demo site is reachable over HTTPS and not
  rate-limiting; `TASK-002` and `TASK-014` cannot be completed offline, and no
  stub or recorded-DOM fallback is planned (`artifacts/architecture.md` § Risks).
- **Assumption:** a local browser matching `browser` and network access to the
  WebDriverManager driver cache are available on the executing machine.
- **Assumption:** parallel scenario execution is **not** expected to work from
  this plan. `ThreadLocal` is thread-safe by design (`AD-005`), but the JUnit 4
  runner does not run scenarios in parallel; enabling it is out of scope
  (`artifacts/architecture.md` § Scalability, `DR-009`).

## Task List

### TASK-001: Bootstrap the Maven project and package skeleton

- **Status:** Done — implemented and committed as `63ff6ea`. Files: `pom.xml`;
  `src/test/java/com/orangehrm/{config,driver,hooks,pages,stepdefinitions,runner}`
  and `src/test/resources/features` directories. Validation: `mvn clean
  install -DskipTests` → BUILD SUCCESS; `mvn dependency:tree` resolved every
  declared dependency.
- **Priority:** High
- **FR/NFR:** FR-001 (enabling)
- **Architecture section:** § Technology Choices (`AD-001`), § Deployment, § Component Diagram (Build and execution)
- **Depends on:** None
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Create `pom.xml` at the repository root targeting JDK 11 with
  the `AD-001` stack: Selenium WebDriver 4.x, Cucumber-JVM 7.x
  (`cucumber-java`, `cucumber-junit`), JUnit 4, WebDriverManager 5.x, and SLF4J
  with a simple binding — all in `test` scope, since there is no `src/main`.
  Configure Surefire to include only `**/TestRunner.java` and `**/*Test.java`
  per project convention. Create the empty package skeleton
  `src/test/java/com/orangehrm/{config,driver,hooks,pages,stepdefinitions,runner}`
  and `src/test/resources/features`. No framework logic in this task.
- **Expected output:** `pom.xml`; the `src/test/java` + `src/test/resources`
  directory skeleton.
- **Validation:** `mvn clean install -DskipTests` completes successfully on
  JDK 11+ from the repository root; `mvn dependency:tree` resolves every declared
  dependency; Surefire's configured includes match the two documented patterns.

### TASK-002: Confirm live-DOM selectors for the six FR-001 controls (spike)

- **Status:** Done — recorded (not committed as production code) as `f5ad120`.
  Full selector inventory is in that commit message; consumed by TASK-007's
  `LoginPage` locator constants. Validation: a throwaway headless-Chrome
  spike (deleted before commit) confirmed each of the six locators resolved
  to exactly one visible element, and both placeholder attributes read the
  literal values `"Username"` / `"Password"`.
- **Priority:** High
- **FR/NFR:** FR-001 (`AC-001`)
- **Architecture section:** § Assumptions and Constraints (demo-site DOM not inspected), § Open Questions (exact selectors), § Risks and Mitigations (markup-change risk)
- **Depends on:** None
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Resolves `DR-003`. Inspect the **live** login page at the
  `AC-001` URL and determine a locator for each of the six `FR-001` controls —
  logo/branding banner, Username field, Password field, "Login" button, "Forgot
  your password?" link, and the footer (copyright notice and social links) —
  plus the two `placeholder` attributes the scenario asserts. Prefer stable,
  hand-authored class fragments or accessible attributes over generated or
  utility class chains (`artifacts/architecture.md` § Risks). Do **not** reuse a
  selector remembered from a different OrangeHRM build, and do not guess: this
  task exists so that no later task has to. Record the inspection date, browser,
  and observed site build alongside each locator. This task writes no production
  code — its deliverable is the verified inventory that `TASK-007` consumes.
- **Expected output:** a selector inventory (control name → locator strategy →
  uniqueness and visibility result → inspection date / browser / observed site
  build), recorded in the `TASK-002` commit message and carried into
  `LoginPage`'s locator comments in `TASK-007`.
- **Validation:** each of the six locators resolves to **exactly one** visible
  element on the live page, and each placeholder attribute returns the literal
  value `FR-001` quotes (`"Username"`, `"Password"`); a reviewer can re-run every
  locator from the inventory and reproduce the same single-match result.

### TASK-003: Declare the environment in config.properties

- **Status:** Done — implemented and committed as `332e782`. Files:
  `src/test/resources/config.properties`. Validation: all named keys present
  with documented defaults; `base.url` is `https` and matches the `AC-001`
  URL exactly; no real secret committed (no credential keys added, per
  scope).
- **Priority:** High
- **FR/NFR:** FR-001 (supplies target URL and timeouts)
- **Architecture section:** § Key Components (`config.properties`), § Technology Choices (`AD-004`)
- **Depends on:** TASK-001
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Add `src/test/resources/config.properties` declaring
  `base.url` (the `https://.../web/index.php/auth/login` URL named in `AC-001`),
  `browser`, `headless`, `explicit.wait`, and `page.load.timeout`. Use an
  `https` `base.url` so the `AD-004` guard passes by default. No credential keys
  are required by `FR-001`/`@TS_LOG_001`; if placeholder credential keys are
  added for future stories, they must carry no real secret.
- **Expected output:** `src/test/resources/config.properties`.
- **Validation:** every key the architecture's component table names is present
  with a documented default; `base.url` uses the `https` scheme and matches the
  `AC-001` URL exactly; no real secret is committed.

### TASK-004: Implement ConfigReader with resolution precedence and the HTTPS fail-closed guard

- **Status:** Done — implemented and committed as `0d8cb1c`. Files:
  `src/test/java/com/orangehrm/config/ConfigReader.java`. Validation:
  compiles under `mvn clean install -DskipTests`; behaviour verified by
  TASK-012's `ConfigReaderTest` (7/7 passing).
- **Priority:** High
- **FR/NFR:** FR-001 (supplies target URL and timeouts)
- **Architecture section:** § Technology Choices (`AD-004`), § Key Components (`config.ConfigReader`), § Security Considerations (Transport, Secret hygiene)
- **Depends on:** TASK-003
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Implement `com.orangehrm.config.ConfigReader` as a
  thread-safe singleton resolving every key with `-D` system property →
  environment variable (key upper-cased, `.` → `_`) → `config.properties`
  precedence. Expose typed accessors for `base.url`, `browser`, `headless`,
  `explicit.wait`, and `page.load.timeout`. `getBaseUrl()` **fails closed**
  unless the scheme is `https`, with a documented, logged opt-out
  (`allow.insecure.base.url=true`) for a local non-TLS sandbox only. Fail closed
  on a missing key and treat a malformed or scheme-less URL as a guard failure.
  Never log a resolved value that could be a secret.
  **Traceability note (`DR-001`):** this guard is planned solely as the `AD-004`
  project convention. It is **not** traced to `NFR-002`, which has no statement
  or acceptance criterion in any approved artifact — do not add an `NFR-002`
  trace comment to this code.
- **Expected output:** `src/test/java/com/orangehrm/config/ConfigReader.java`.
- **Validation:** compiles under `mvn clean install -DskipTests`; behaviour is
  verified by `TASK-012`'s unit tests rather than by inspection.

### TASK-005: Implement DriverManager (ThreadLocal WebDriver lifecycle)

- **Status:** Done — implemented and committed as `36d8122`. Files:
  `src/test/java/com/orangehrm/driver/DriverManager.java`. Validation:
  `mvn test` launched exactly one Chrome session per scenario with clean
  teardown; `-Dbrowser=safari` produced a clear `IllegalArgumentException`;
  `grep -rn "implicitlyWait" src/` found no call, only the explanatory
  comment.
- **Priority:** High
- **FR/NFR:** FR-001 (supporting)
- **Architecture section:** § Technology Choices (`AD-005`), § Key Components (`driver.DriverManager`), § Reliability and Observability
- **Depends on:** TASK-004
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Implement `com.orangehrm.driver.DriverManager` holding the
  session in a `ThreadLocal<WebDriver>`, supporting `chrome | firefox | edge`
  resolved from `ConfigReader`, honouring `headless`, and using WebDriverManager
  to resolve driver binaries. Apply the configured page-load timeout and
  deliberately apply **no implicit wait** (`AD-003`). Provide create, peek (for
  failure screenshots without resurrecting a session), and quit operations;
  `quit` must remove the `ThreadLocal` entry. If initialization fails partway,
  quit the browser it started but had not yet registered, so no orphan process
  is left behind. Log the resolved browser, never a configuration secret.
- **Expected output:** `src/test/java/com/orangehrm/driver/DriverManager.java`.
- **Validation:** a scenario run launches exactly one session and leaves no
  browser or driver process after teardown; forcing an unsupported `browser`
  value produces a clear, actionable failure; no implicit wait is set anywhere
  (verifiable by inspection plus the absence of an
  `implicitlyWait` call in the codebase).

### TASK-006: Implement BasePage explicit-wait helpers

- **Status:** Done — implemented and committed as `8b1522a`. Files:
  `src/test/java/com/orangehrm/pages/BasePage.java`. Validation: compiles
  under `mvn clean install -DskipTests`; exercised end-to-end by the
  passing `@TS_LOG_001` run (TASK-011).
- **Priority:** High
- **FR/NFR:** FR-001 (supporting)
- **Architecture section:** § Technology Choices (`AD-003`), § Key Components (`pages.BasePage`), § Reliability and Observability (Failure handling)
- **Depends on:** TASK-004
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Implement the abstract `com.orangehrm.pages.BasePage` with the
  shared explicit-wait helpers every page object reuses: wait-for-visible (with
  the default `explicit.wait` and a short per-control override), a
  visibility predicate that **catches `TimeoutException` and returns `false`** so
  "not displayed" is a reportable first-class outcome instead of a thrown error
  (this is what makes `AD-006`'s collect-all-missing behaviour possible), a
  navigate helper, and an attribute reader. Timeouts come from `ConfigReader`,
  never hard-coded. No implicit wait.
- **Expected output:** `src/test/java/com/orangehrm/pages/BasePage.java`.
- **Validation:** the visibility predicate returns `false` within its own
  timeout for a deliberately bogus locator and does not propagate a
  `TimeoutException`; no helper hard-codes a timeout value.

### TASK-007: Implement LoginPage — FR-001 locators, missing-controls check, placeholder readers

- **Status:** Done — implemented and committed as `f5329e8`, using the
  TASK-002 selector inventory (unblocked). Files:
  `src/test/java/com/orangehrm/pages/LoginPage.java`. Validation: against
  the live page, `findMissingLoginControls()` returns an empty list (proven
  by the passing `@TS_LOG_001` run, TASK-011); placeholder readers return
  `"Username"` / `"Password"`; no assertion and no `Thread.sleep` in the
  class. Independent evaluation re-verified by deliberately pointing
  `FORGOT_PASSWORD_LINK` at a nonexistent class, re-running, confirming the
  failure named exactly `[Forgot your password? link]` and nothing else,
  then reverting via `git checkout --` and re-confirming the scenario
  passes again.
- **Priority:** High
- **FR/NFR:** FR-001 (`AC-001`)
- **Architecture section:** § Key Components (`pages.LoginPage`), § Technology Choices (`AD-002`, `AD-006`), § Data Flow
- **Depends on:** TASK-002, TASK-006
- **Blocked by:** TASK-002 — locator constants cannot be finalized until the live
  DOM has been inspected (`DR-003`)
- **Target agent:** Implementation Agent
- **Description:** Implement `com.orangehrm.pages.LoginPage` extending
  `BasePage` as the **sole owner** of all `FR-001` DOM knowledge, using only the
  selectors confirmed by `TASK-002`. Provide: (a) `open(baseUrl)`, which
  navigates and then waits for the Username field, establishing `AC-001`'s "page
  finishes loading" precondition before any control check runs (`AD-003`);
  (b) a missing-controls check that evaluates all six required controls
  **independently** — one visibility evaluation per control, each with its own
  short wait, no short-circuiting — and returns the human-readable names of the
  controls that are not visible (`AD-006`); and (c) placeholder readers that
  return the Username and Password `placeholder` attribute values. The page class
  contains **no assertions** — it reports state, the step layer judges it. Carry
  `// FR-001` trace comments on the locators and the check method.
- **Expected output:** `src/test/java/com/orangehrm/pages/LoginPage.java`.
- **Validation:** against the live page the check returns an empty list; with one
  locator deliberately pointed at a nonexistent element the returned list names
  exactly that one control and nothing else (proving independent evaluation);
  the placeholder readers return `"Username"` and `"Password"`; no assertion and
  no `Thread.sleep` appears in the class.

### TASK-008: Implement Hooks — per-scenario driver lifecycle and failure screenshots

- **Status:** Done — implemented and committed as `943ca1b`. Files:
  `src/test/java/com/orangehrm/hooks/Hooks.java`. Validation: the passing
  `@TS_LOG_001` run (TASK-011) attached no screenshot and left no browser
  process; the deliberately-broken-locator run above (TASK-007) attached a
  screenshot on failure while still reporting the original assertion
  message as the cause.
- **Priority:** High
- **FR/NFR:** FR-001 (diagnostics and isolation)
- **Architecture section:** § Key Components (`hooks.Hooks`), § Technology Choices (`AD-005`, `AD-007`), § Reliability and Observability (Isolation, Failure handling)
- **Depends on:** TASK-005
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Implement `com.orangehrm.hooks.Hooks` with a Cucumber
  `@Before` that launches the driver before the first step and an `@After` that
  attaches a PNG screenshot to the scenario **only on failure** and always quits
  the driver. Screenshot capture is best-effort: wrap it in its own `try/catch`
  and quit the driver in a `finally`, so neither a crashed session nor a
  screenshot failure can mask the original scenario failure or leak a browser.
  Hooks run per scenario, not per suite, guaranteeing a fresh isolated session.
  Log scenario start, finish, and status via SLF4J; log no secret.
- **Expected output:** `src/test/java/com/orangehrm/hooks/Hooks.java`.
- **Validation:** a passing scenario attaches no screenshot and leaves no browser
  process; a deliberately failed scenario attaches a screenshot **and** still
  reports the original assertion failure as the cause; a scenario whose session
  has already died still tears down cleanly without a secondary exception
  replacing the first.

### TASK-009: Author login.feature scenario @TS_LOG_001 for AC-001

- **Status:** Done — implemented and committed as `56e5010`. Files:
  `src/test/resources/features/login.feature`. Validation: `mvn test`
  parsed and executed the file with no undefined/ambiguous step; exactly
  one scenario; `# FR-001` comment and `@TS_LOG_001` tag present.
- **Priority:** High
- **FR/NFR:** FR-001 (`AC-001`)
- **Architecture section:** § Recommended Architecture (Specification layer), § Key Components (`login.feature`), § Technology Choices (`AD-001`, `AD-006`)
- **Depends on:** TASK-001
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Author `src/test/resources/features/login.feature` with a
  `Background` step placing the user on the login page, and one scenario tagged
  `@TS_LOG_001 @positive` that states `AC-001` in business-readable Gherkin: all
  required login controls are displayed, and the Username and Password
  placeholders read `"Username"` and `"Password"`. Carry an inline `# FR-001`
  trace comment. Keep it to this single scenario — no credential, login-outcome,
  validation, password-masking, or password-recovery scenario, all of which are
  explicitly out of scope in `artifacts/requirements.md` § Out of Scope. Step
  text must be declarative (what is displayed), never mention locators,
  browsers, or waits.
- **Expected output:** `src/test/resources/features/login.feature`.
- **Validation:** the file parses as valid Gherkin; it contains exactly one
  scenario; the scenario's `Then` steps map one-to-one onto `AC-001`'s stated
  outcomes; the `# FR-001` comment and `@TS_LOG_001` tag are present.

### TASK-010: Implement LoginSteps — assertions binding @TS_LOG_001 to LoginPage

- **Status:** Done — implemented and committed as `ef76554`. Files:
  `src/test/java/com/orangehrm/stepdefinitions/LoginSteps.java`. Validation:
  `grep` found no `org.openqa.selenium` import or `By.` reference; every
  step bound with no undefined/ambiguous step; the deliberate-break run
  (TASK-007) produced the message `Missing login control(s): [Forgot your
  password? link]`, naming the specific missing control.
- **Priority:** High
- **FR/NFR:** FR-001 (`AC-001`)
- **Architecture section:** § Recommended Architecture (Glue layer), § Key Components (`stepdefinitions.LoginSteps`), § Technology Choices (`AD-002`, `AD-006`)
- **Depends on:** TASK-004, TASK-007, TASK-009
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Implement `com.orangehrm.stepdefinitions.LoginSteps` binding
  every step in `@TS_LOG_001`: the `Background` step resolves `base.url` through
  `ConfigReader` and calls `LoginPage.open(...)`; the controls step fails when
  `LoginPage`'s missing-controls check returns a non-empty list, embedding **the
  missing control names** in the assertion message so a failure is diagnosable
  without a re-run (`AD-006`); the placeholder steps assert equality against
  `"Username"` and `"Password"`. Contains assertions and orchestration only —
  **zero Selenium API calls** and zero locators (`AD-002`). Tag the `FR-001`
  step methods with `// FR-001` trace comments.
- **Expected output:** `src/test/java/com/orangehrm/stepdefinitions/LoginSteps.java`.
- **Validation:** no `org.openqa.selenium` import or `By.` reference appears in
  the file; every step in `login.feature` binds (no undefined or ambiguous step
  when the runner executes); an induced failure message names the specific
  missing control(s).

### TASK-011: Implement TestRunner and wire up multi-format reporting

- **Status:** Done — implemented and committed as `295f28b`. Files:
  `src/test/java/com/orangehrm/runner/TestRunner.java`; Surefire includes
  (TASK-001's `pom.xml`). Validation: `mvn test` → 1 scenario, 4 steps, all
  passed; `mvn test -Dcucumber.filter.tags="@TS_LOG_001"` ran exactly that
  scenario; `target/cucumber-reports/{cucumber.html,cucumber.json,
  cucumber.xml}` all produced; no step undefined.
- **Priority:** High
- **FR/NFR:** FR-001 (execution and reporting)
- **Architecture section:** § Key Components (`runner.TestRunner`), § Technology Choices (`AD-001`, `AD-007`), § Reliability and Observability (Observability)
- **Depends on:** TASK-008, TASK-009, TASK-010
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Implement `com.orangehrm.runner.TestRunner` as the JUnit 4 +
  Cucumber entry point (`@RunWith(Cucumber.class)`) pointing at
  `src/test/resources/features`, binding the `stepdefinitions` and `hooks` glue
  packages, and emitting `pretty`/`summary` console output plus HTML, JSON, and
  JUnit-XML reports into `target/` (JSON and XML are the machine-readable inputs
  the Verify Agent consumes in `TASK-014`). Confirm tag filtering works via
  `-Dcucumber.filter.tags`.
- **Expected output:** `src/test/java/com/orangehrm/runner/TestRunner.java`;
  report plugins configured.
- **Validation:** `mvn test` discovers and executes `@TS_LOG_001`;
  `mvn test -Dcucumber.filter.tags="@TS_LOG_001"` runs exactly that scenario;
  HTML, JSON, and JUnit-XML reports are produced under `target/`; no step is
  reported undefined.

### TASK-012: Add ConfigReader unit tests for precedence and the HTTPS guard

- **Status:** Done — implemented and committed as `75079fc`. Files:
  `src/test/java/com/orangehrm/config/ConfigReaderTest.java`; `pom.xml`
  (added the test-scoped `system-rules` dependency and a Surefire
  `--add-opens` argLine so the `EnvironmentVariables` `@Rule` can
  deterministically set an environment variable for the precedence test —
  a deviation from the original AD-001 stack table, justified in the
  commit message). Validation: `mvn test` ran all 7 cases, all passing;
  `mvn test -Dtest=ConfigReaderTest` run twice in isolation produced
  identical results with no leaked system property; no test cites
  `NFR-002`.
- **Priority:** Medium
- **FR/NFR:** FR-001 (supporting) — guard behaviour traces to `AD-004`, not to any approved NFR (`DR-001`)
- **Architecture section:** § Technology Choices (`AD-004`, trade-off note on singleton state in unit tests), § Security Considerations (Transport, Input validation)
- **Depends on:** TASK-004
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Add `ConfigReaderTest` (matching Surefire's `**/*Test.java`
  include) covering `ConfigReader`'s security-relevant behaviour: `-D` → env →
  file precedence; `getBaseUrl()` rejecting a non-`https` URL; the documented
  insecure opt-out being honoured only when explicitly set; and failing closed on
  a missing key or malformed URL. Because `ConfigReader` is a singleton over
  global system properties, each test must save and restore any property it
  mutates so tests stay order-independent and deterministic
  (`.claude/rules/tests.md`). Reference `AD-004` in test names or one-line
  comments. **Do not** cite `NFR-002` — it has no definition in any approved
  artifact (`DR-001`).
- **Expected output:** `src/test/java/com/orangehrm/config/ConfigReaderTest.java`.
- **Validation:** `mvn test` runs the class via Surefire and all cases pass;
  running the class repeatedly and in isolation yields identical results (no
  leaked system property); no test cites `NFR-002`.

### TASK-013: Code review against the project rules

- **Status:** Not started
- **Priority:** High
- **FR/NFR:** FR-001 (quality gate)
- **Architecture section:** § SDLC Pipeline Components (Step 6, `code-review`)
- **Depends on:** TASK-011, TASK-012
- **Blocked by:** None
- **Target agent:** Review Agent
- **Description:** Review the new framework against
  `.claude/rules/code-quality.md` and `.claude/rules/tests.md`, plus the
  architecture's own constraints: no Selenium call or locator in
  `LoginSteps` (`AD-002`); no implicit wait and no `Thread.sleep` anywhere
  (`AD-003`); controls evaluated independently with all missing names reported
  (`AD-006`); no hard-coded or logged credential and every configuration value
  resolved through `ConfigReader` (`DR-007`); `// FR-001` / `# FR-001` trace
  comments present on the `FR-001` code paths; no `NFR-002` citation anywhere
  (`DR-001`); no `DashboardPage`/`ResetPasswordPage` created (`DR-006`).
- **Expected output:** review findings and any resulting fixes, each citing a
  file plus `FR-001` or the relevant `AD-###`.
- **Validation:** every finding is either fixed or explicitly accepted with a
  rationale; no `Critical` or unresolved `High` finding remains before
  `TASK-014` starts.

### TASK-014: Execute the suite and verify AC-001 end to end

- **Status:** Not started
- **Priority:** High
- **FR/NFR:** FR-001 (`AC-001`)
- **Architecture section:** § SDLC Pipeline Components (Step 7, `verify-implementation`), § Technology Choices (`AD-007`), § Deployment (Run)
- **Depends on:** TASK-013
- **Blocked by:** External dependency — the live demo site must be reachable over
  HTTPS and a matching local browser must be available; `@TS_LOG_001` cannot run
  offline and has no stub fallback (`DR-008`)
- **Target agent:** Verify Agent
- **Description:** Run `mvn clean test` from the repository root and confirm
  `@TS_LOG_001` passes, demonstrating `AC-001`: all six controls visible and both
  placeholders correct. Record the command, environment (browser, headless,
  resolved `base.url`), pass/fail counts, runtime, and the report locations.
  Confirm the HTML/JSON/JUnit-XML reports exist and that a failure would carry a
  screenshot. Write `artifacts/verification-report.md` tracing the result back to
  `FR-001`/`AC-001`. Report any failure as a finding rather than retrying it away
  (`.claude/rules/tests.md` determinism — no retry mechanism exists by design).
- **Expected output:** `artifacts/verification-report.md`; Cucumber reports under
  `target/`.
- **Validation:** `@TS_LOG_001` passes with zero undefined or skipped steps;
  `ConfigReaderTest` passes; the report maps `AC-001`'s outcomes to the executed
  assertions, and any deviation is recorded as an explicit finding.

### TASK-015: Open the pull request and record the changelog

- **Status:** Not started
- **Priority:** Medium
- **FR/NFR:** FR-001
- **Architecture section:** § SDLC Pipeline Components (Step 8, `create-pr`)
- **Depends on:** TASK-014
- **Blocked by:** None
- **Target agent:** PR Agent
- **Description:** Open the pull request from `test/pipeline-rerun` to `main`
  describing the new framework, with a Changes Made section and Reviewer
  Checklist citing `FR-001`/`AC-001` and the `TASK-###` range, plus a
  traceability matrix (`FR-001` → `AD-###` → `TASK-###` → code → test). Write
  `artifacts/CHANGELOG.md`. Carry forward as known limitations, not as silent
  omissions: `NFR-002` undefined (`DR-001`), the stale file/line citation in
  `requirements.md` (`DR-005`), no CI workflow (`DR-004`), and parallel execution
  not enabled (`DR-009`).
- **Expected output:** pull request; `artifacts/CHANGELOG.md`.
- **Validation:** the PR body traces every change to `FR-001` and a `TASK-###`;
  the verification result from `TASK-014` is linked; all carried-forward
  limitations are listed explicitly.

## Parallelization Opportunities

- **`TASK-001` and `TASK-002` can run concurrently from the start.** Neither has
  a prerequisite: `TASK-002` is a live-site inspection that needs no build, so
  the `DR-003` selector unknown is retired while the project is being
  bootstrapped — this is the single biggest schedule win in the plan.
- **`TASK-003` and `TASK-009` can run concurrently once `TASK-001` completes.**
  The feature file (specification layer) shares nothing with
  `config.properties` (infrastructure layer).
- **`TASK-005`, `TASK-006`, and `TASK-012` can all run concurrently once
  `TASK-004` completes.** `DriverManager`, `BasePage`, and the `ConfigReader`
  unit tests touch different files and only read `ConfigReader`'s API.
- **`TASK-007` and `TASK-008` can run concurrently** once their respective
  dependencies (`TASK-002`+`TASK-006`, and `TASK-005`) are met — different
  layers, no shared file.
- **Not parallelizable:** `TASK-010` needs both `LoginPage`'s API and the
  Gherkin step text to be stable, and `TASK-011` needs all glue to exist before
  it can bind and report. `TASK-013` → `TASK-014` → `TASK-015` are sequential
  pipeline gates by definition.

## Blocked Tasks

| Task | Blocked by | Unblock criteria |
| --- | --- | --- |
| `TASK-007` (LoginPage) | `TASK-002` — live-DOM selectors never inspected (`DR-003`) | `TASK-002`'s selector inventory is recorded and every one of the six controls resolves to exactly one visible element on the live page. Until then, `LoginPage`'s locator constants must not be written — a guessed selector is explicitly disallowed. |
| `TASK-010` (LoginSteps) | `TASK-007`, `TASK-009` (ordinary dependencies, not an open decision) | `LoginPage`'s public API (missing-controls check, placeholder readers, `open`) and `login.feature`'s step text are both committed and stable. |
| `TASK-014` (verification run) | External dependency: live demo site reachable over HTTPS; local browser present; WebDriverManager able to resolve a driver binary (`DR-008`) | A manual navigation to the `AC-001` URL returns the login page and `mvn test` can launch the configured browser. A sustained outage has no mitigation by design (no stub layer) and must be reported, not retried around. |
| `TASK-015` (PR) | `TASK-013`, `TASK-014` | No `Critical`/unresolved `High` review finding, and `artifacts/verification-report.md` records `@TS_LOG_001` passing. |

No task is blocked by an unresolved **design** decision. All four design-review
open items (`DR-001`, `DR-003`, `DR-004`, `DR-005`) have an explicit disposition
in `artifacts/design-review.md` and none gates the start of `TASK-001`.

## Risks and Open Questions

**Carried forward from the design review (not re-decided here):**

- **`DR-001` — `NFR-002` (HTTPS transport) has no definition in any approved
  artifact.** `requirements.md` names it only inside § Out of Scope as a pointer
  to work "tracked separately"; no `NFR-001` exists either. This plan therefore
  **invents no `NFR-002` task**: the HTTPS fail-closed guard is planned in
  `TASK-004` and tested in `TASK-012` purely as the `AD-004` project convention,
  and `TASK-013` explicitly checks that no code cites `NFR-002`. Consequence to
  accept: Step 7 cannot verify an HTTPS requirement against an approved
  requirement, because none exists. **Open for the user:** amend
  `requirements.md` in a future Step 1 revision with a measurable `NFR-002`, or
  formally record it as an inherited convention. The Planner Agent cannot decide
  this — only Step 1 may define a requirement.
- **`DR-003` — live-DOM selectors were never inspected.** Addressed structurally
  by making `TASK-002` an early, no-prerequisite task that `TASK-007` is blocked
  by, rather than an assumption buried inside a coding task. No task in this plan
  states a selector string.
- **`DR-005` — `requirements.md` § Open Questions cites
  `LoginSteps.java` lines 155/164/170, a file that does not exist on this
  branch.** Non-blocking and unchanged by this plan (`FR-001`/`AC-001`'s
  substance is unaffected), but no agent should read that citation as evidence
  that a reference implementation exists to check against. Flagged for a future
  Step 1 revision; carried into the PR's limitations by `TASK-015`.
- **`DR-004` — no CI workflow.** Local, on-demand verification is the accepted
  model for this run, so no CI task is planned. The design remains CI-ready
  (`headless`, `-D` injection, JUnit-XML).
- **`DR-006` — `DashboardPage`/`ResetPasswordPage`** are out-of-scope
  placeholders; deliberately no task.
- **`DR-009` — parallel execution** is designed for (`ThreadLocal`) but not
  delivered by the JUnit 4 runner. No task enables it; no downstream agent should
  assume it works.

**Delivery risks specific to this plan:**

| Risk | Likelihood | Impact | Mitigation |
| --- | --- | --- | --- |
| A demo-site markup change invalidates `TASK-002`'s selector inventory between the spike and `TASK-007` (or between `TASK-007` and `TASK-014`) | Medium | High — `@TS_LOG_001` fails with no product defect | Keep `TASK-002` and `TASK-007` close together in the sequence; prefer stable hand-authored class fragments over generated chains; all locators confined to one file, so the fix is one-file. Record the observed site build in the inventory so drift is detectable. |
| Demo site unreachable or rate-limiting during `TASK-002` or `TASK-014` | Medium | High — those tasks cannot complete | Explicit timeouts fail fast with a clear message; the failure screenshot distinguishes "site down" from "control missing". No stub fallback by design — report, do not retry. |
| Greenfield scope creep: implementing out-of-scope login behaviour (AC2/AC3/AC4, password masking) because the framework structure invites it | Medium | Medium — unapproved scope, extra review burden | `TASK-009` is explicitly limited to one scenario; `TASK-013` checks that no out-of-scope page object or scenario was added. |
| `ConfigReader`'s singleton + system-property mutation makes `TASK-012`'s tests order-dependent | Medium | Medium — flaky unit tests undermine the only non-UI test class | `TASK-012` mandates save/restore of every mutated property and validates by running the class both repeatedly and in isolation. |
| Browser auto-update outpaces the cached driver binary | Low | Medium | WebDriverManager resolves matching binaries per run; `TASK-005` logs the resolved browser and quits a half-initialized session. |

**Open question raised by this step (non-blocking):** none. All planning
decisions needed to start `TASK-001` are settled; the only genuinely open items
are `DR-001` and `DR-004`, both owned by the user and neither on `FR-001`'s
critical path.

## Recommended First Task for Implementation Agent

`TASK-001` — Bootstrap the Maven project and package skeleton.

Start `TASK-002` (live-DOM selector confirmation) **in parallel**: it has no
prerequisite, it is the plan's only genuine unknown (`DR-003`), and `TASK-007`
is blocked until it completes. Retiring that unknown while the build is being
bootstrapped keeps it off the critical path.
