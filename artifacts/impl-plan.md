# Implementation Plan

## Overview

This plan turns the approved architecture (`artifacts/architecture.md`, commit
`b448cb4`, amended by `26bf9c5`) and the three accepted design-review findings routed
to Step 4 into seven dependency-ordered tasks. There is no greenfield construction:
the four-layer test architecture (Specification → Glue → Page Objects →
Infrastructure) is already implemented, `FR-001` is already covered end-to-end by
`@TS_LOG_008`, and no `Critical` or `High` design-review finding exists. The work is
therefore **assertion strictness and traceability hygiene**, not new capability.

Two delivery threads:

1. **Epic A — Close `FR-001`'s two assertion gaps (`TASK-012`, `TASK-013`).** Both are
   partial-regression blind spots against the approved wording of `AC-001`: "social
   link**s**" is currently satisfied by one visible link (`DR-019`), and the two
   literal labels `FR-001` quotes verbatim — `"Login"` and `"Forgot your password?"` —
   are located structurally but never text-asserted (`DR-023`). Each fix is local to
   `LoginPage` plus the step layer, adds no scope, and is independently verifiable.
   These are the highest-priority tasks because they are the only ones that increase
   `FR-001`'s defect-detection power.
2. **Epic B — Restore one-scenario-per-requirement (`TASK-014`).** The `NFR-002`
   HTTPS assertion is moved out of `@TS_LOG_008` — the single scenario `FR-001` traces
   to — into a new `@TS_LOG_013` scenario inheriting the same `Background` (`DR-020`).
   A split, never a deletion; coverage is preserved at the same pre-submit point.

Epic A and Epic B both edit `login.feature`, so they are sequenced rather than
parallelised (see [Parallelization Opportunities](#parallelization-opportunities)).
`TASK-015` through `TASK-018` are the downstream gates: review, verification, artifact
doc-sync, and PR. Delivery sequence is `TASK-012` → `TASK-013` → `TASK-014` →
`TASK-015` → `TASK-016` → `TASK-017` → `TASK-018`.

**ID allocation.** Per the `DR-018` ruling recorded in `artifacts/architecture.md`
§ Traceability, task IDs start at **`TASK-012`**. `TASK-001`–`TASK-011` are permanently
retired: they are cited by live code comments (`ConfigReaderTest.java:19,21,27,114`,
`pom.xml:97`) whose defining documents are `Not Found`, and are never reused,
renumbered, or reinterpreted by this plan.

## Source References

- Architecture: `artifacts/architecture.md`
- Requirements: `artifacts/requirements.md`
- Design review: `artifacts/design-review.md` (findings `DR-018`–`DR-029`; `DR-019`,
  `DR-020`, `DR-023` routed here, with `DR-026` folded into `DR-019`)
- Traceability conventions: `.claude/skills/sdlc-traceability/SKILL.md`

## Assumptions and Constraints

**Confirmed constraints carried forward from the architecture (do not re-litigate):**

- Step definitions contain **zero** Selenium calls: locators and waits live only in
  `pages/*`, the step layer only asserts. Every task below preserves this split —
  page objects gain *readers*, step definitions gain *assertions*.
- Explicit waits only; **no** implicit wait anywhere.
- The missing-controls group keeps its deliberately short 3 s per-control budget
  (`MISSING_CONTROLS_TIMEOUT_SECONDS`), so the all-missing worst case stays bounded at
  ~21 s. `BasePage.waitForAllVisible(By)` currently uses the 15 s `explicit.wait` and
  has **zero** callers; reusing it as-is inside `findMissingLoginControls()` would push
  that worst case to ~33 s and contradict the `DR-003` rationale. `TASK-012` must
  therefore introduce an explicit-timeout, count-aware variant — not reuse the
  existing signature (`DR-019` + `DR-026`).
- The footer is asserted **transitively and sufficiently** by its two descendant-scoped
  checks. No `.orangehrm-login-footer` container locator is added (`DR-021`, closed).
- `FR-001` is verified by exactly one scenario. `TASK-014` exists to keep that true.
- The system under test is an external, read-only public demo site: no stub, no
  recorded-DOM fallback, no retries (deliberate, per `.claude/rules/tests.md`
  determinism). Tasks cannot be validated offline.
- Parallel execution is **prepared but not enabled** (JUnit 4 runner). No task assumes
  parallel scenario execution.

**Planning assumptions specific to this plan:**

- The live demo footer renders **more than one** social link. `≥ 2` is the
  requirement-derived floor for `AC-001`'s plural and is what `TASK-012` encodes; the
  exact live count must be confirmed against the DOM during implementation rather than
  hard-coded from this document (`DR-019`).
- `NFR-002` has **no formal definition in any artifact** (`DR-024`, open user
  decision). `TASK-014` is deliberately scoped so the existing `# NFR-002` code-comment
  convention — already used at `login.feature:15,58`, `ConfigReader`, `ConfigReaderTest`
  and `LoginSteps` — is the only traceability it needs. This plan does **not** invent an
  `NFR-002` statement, target, or acceptance criterion.
- No task touches `pom.xml`, `.claude/`, `.mcp.json`, `.gitignore`, or
  `artifacts/ORCHESTRATION_LOG.md`. No task adds, removes, or renumbers an existing ID
  citation in `src/`.

**Explicitly not planned** (closed or deferred in Step 3 — see
[Risks and Open Questions](#risks-and-open-questions)): `DR-021`, `DR-025`, `DR-027`,
`DR-028`, `DR-029` (all closed as accepted), `DR-022` (CI workflow — open user
decision), `DR-024` (the `NFR-002` definition itself — open user decision), and
`DR-026` as a standalone item (folded into `TASK-012`).

## Task List

### TASK-012: Tighten the footer social-link assertion to a minimum visible count

- **Status:** Done — implemented and committed as `58b5691` (`src/test/java/com/orangehrm/pages/BasePage.java`,
  `src/test/java/com/orangehrm/pages/LoginPage.java`). Live DOM check confirmed 4
  visible footer social links at implementation time, above the `MIN_FOOTER_SOCIAL_LINKS = 2`
  floor, so no escalation was needed. `mvn test -Dcucumber.filter.tags="@TS_LOG_008"`
  passes; negative proof (temporarily raising the floor to 99) failed with the
  expected `footer social-media links (expected at least 99)` message, then reverted.
  Full suite: 11/12 scenarios pass; `@TS_LOG_009` (loading indicator) fails but was
  confirmed pre-existing and unrelated to this task (reproduced on the pre-change
  code, outside `TASK-012`'s scope) — flagged to the user rather than silently fixed.
- **Priority:** High
- **FR/NFR:** FR-001 (AC-001)
- **Architecture section:** § Key Components and Responsibilities
  (`pages/LoginPage.findMissingLoginControls()`, `pages/BasePage`); § Assumptions and
  Constraints (the superseded "at least one social link" bullet); § Risks and
  Mitigations (row: "Only one social link is asserted though `FR-001` says *social
  links*")
- **Depends on:** None
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** `LoginPage.findMissingLoginControls()` currently calls
  `isDisplayed(FOOTER_SOCIAL_LINK, 3)`, which passes on the **first** match, so a
  regression that collapses the footer to a single social link still satisfies
  `@TS_LOG_008` (`DR-019`). Replace that single-element check with a minimum-visible-count
  check:
  1. Add a named constant to `LoginPage` (e.g.
     `MIN_FOOTER_SOCIAL_LINKS`) holding the floor. Use `2` as the
     requirement-derived minimum for `AC-001`'s plural, and **confirm the actual live
     count against the DOM before finalising** — do not raise the floor above what the
     live page renders, and do not hard-code an unverified number.
  2. Add a count-aware helper to `BasePage` that takes an **explicit timeout**, e.g.
     `areAtLeastNVisible(By locator, int minCount, int timeoutSeconds)`, returning
     `false` on `TimeoutException` exactly as `isDisplayed` does, so "too few links" stays
     a first-class reportable outcome rather than an exception.
  3. Resolve `DR-026` in the same change: `BasePage.waitForAllVisible(By)` has zero
     callers and silently uses the 15 s `explicit.wait`. Either give it its first real
     caller via an explicit-timeout overload used by the new helper, or replace it with
     that helper. Whichever is chosen, the task must end with **no uncalled wait helper
     left in `BasePage`** and with the 3 s group budget intact (worst case stays ~21 s,
     not ~33 s).
  4. Keep the control in the missing-controls list with a name that states the
     expectation (e.g. `"footer social-media links (expected at least N)"`), so the
     existing failure message still names exactly what was wrong.
  Do **not** change `FOOTER_SOCIAL_LINK`'s selector, add a footer-container locator
  (`DR-021`), or alter any other control's check.
- **Expected output:** Modified `src/test/java/com/orangehrm/pages/LoginPage.java` and
  `src/test/java/com/orangehrm/pages/BasePage.java`. No feature-file or step-definition
  change is required — the existing `Then all required login controls should be
  displayed` step and its assertion message carry the new check unchanged.
- **Validation:** `mvn test -Dcucumber.filter.tags="@TS_LOG_008"` passes against the
  live demo site. Negative proof that the assertion actually bites: temporarily lower
  the constant's effective comparison (or point the locator at a deliberately
  single-match selector) in a scratch run, confirm the scenario fails and that the
  failure message names the social-links control and the expected count, then revert.
  Confirm via `grep`/IDE search that `BasePage` has no remaining uncalled wait helper,
  and that the per-control timeout used is 3 s, not the 15 s `explicit.wait`.

### TASK-013: Assert the literal "Login" and "Forgot your password?" control labels

- **Status:** Done — implemented and committed as `1d477ad` (`src/test/java/com/orangehrm/pages/LoginPage.java`,
  `src/test/java/com/orangehrm/stepdefinitions/LoginSteps.java`,
  `src/test/resources/features/login.feature`). `getLoginButtonText()` /
  `getForgotPasswordLinkText()` readers added to `LoginPage` alongside the placeholder
  readers (trimmed, whitespace-collapsed); two `// FR-001` `@Then` bindings added to
  `LoginSteps`; two `And` steps with literal values `"Login"` and
  `"Forgot your password?"` added to `@TS_LOG_008`. `mvn test -Dcucumber.filter.tags="@TS_LOG_008"`
  passes (7/7 steps, no `undefined` steps). Negative proof: temporarily changed the
  expected Login-button label to `"Sign in"`; the scenario failed with
  `org.junit.ComparisonFailure: expected:<[Sign ]in> but was:<[Log]in>`, confirming the
  assertion detects a mislabel; reverted before committing. Full suite:
  11/12 scenarios pass (45 steps, 44 passed); `@TS_LOG_009` (loading indicator) fails but
  is the same pre-existing, unrelated failure already flagged in `TASK-012` — not
  reintroduced or worsened by this task, and not fixed here (out of scope).
- **Priority:** High
- **FR/NFR:** FR-001 (AC-001)
- **Architecture section:** § Key Components and Responsibilities (`pages/LoginPage`
  readers, `stepdefinitions/LoginSteps` placeholder-assertion steps); § Risks and
  Mitigations (row: "`FR-001`'s quoted labels … are located structurally but never
  text-asserted")
- **Depends on:** None (logically independent of `TASK-012`; see
  [Parallelization Opportunities](#parallelization-opportunities) for the file-contention
  caveat)
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** `FR-001` quotes two labels verbatim — a `"Login"` submit button and a
  `"Forgot your password?"` link — but both are located purely structurally
  (`button[type='submit']`, `.orangehrm-login-forgot-header`) and their visible text is
  never compared, so a relabelled control ("Sign in", "Reset password") still passes
  `@TS_LOG_008` (`DR-023`). Mirror the existing placeholder pattern exactly — page
  object reads, step layer asserts:
  1. Add `getLoginButtonText()` and `getForgotPasswordLinkText()` readers to
     `LoginPage`, alongside `getUsernamePlaceholder()` / `getPasswordPlaceholder()`.
     Return trimmed / whitespace-normalised text to avoid DOM-whitespace flakiness.
  2. Add two `@Then` bindings to `LoginSteps`, each carrying a `// FR-001` trace comment
     like the three existing `FR-001` steps, asserting the expected label with
     `assertEquals` and the expected value supplied from the feature file as a `{string}`
     parameter (never hard-coded in Java).
  3. Add the two corresponding `And` steps to `@TS_LOG_008` with the literal expected
     values, and extend that scenario's `# FR-001` comment to mention the two labels.
  Keep the structural locators as they are — tightening `button[type='submit']` is not
  in scope; the text assertion is what detects a relabelled or substituted control.
- **Expected output:** Modified `src/test/java/com/orangehrm/pages/LoginPage.java`,
  `src/test/java/com/orangehrm/stepdefinitions/LoginSteps.java`, and
  `src/test/resources/features/login.feature` (`@TS_LOG_008` only).
- **Validation:** `mvn test -Dcucumber.filter.tags="@TS_LOG_008"` passes with the two
  new steps bound (no `undefined step` in the Cucumber summary — a silently unbound step
  is the known failure mode of this layer). Negative proof: temporarily change an
  expected value in the feature file to a wrong label, confirm the scenario fails with an
  `assertEquals` message showing expected vs actual, then revert.

### TASK-014: Split the NFR-002 HTTPS assertion out of @TS_LOG_008 into @TS_LOG_013

- **Status:** Done — implemented and committed as `f45c376`
  (`src/test/resources/features/login.feature` only; no Java changes). Removed the
  trailing `# NFR-002` comment and `And the current page should be served over HTTPS`
  step from `@TS_LOG_008`; added a new `@TS_LOG_013 @security` scenario "Login page is
  served over HTTPS" immediately after it, carrying the relocated (reworded)
  `# NFR-002` comment and a single `Then the current page should be served over HTTPS`
  step, relying solely on the inherited `Background`. `@TS_LOG_001`'s own HTTPS
  step/comment left untouched; `the_current_page_should_be_served_over_https()` in
  `LoginSteps.java` reused unchanged. Validation:
  `mvn test -Dcucumber.filter.tags="@TS_LOG_013"` — 1 scenario, 2 steps, passed;
  `mvn test -Dcucumber.filter.tags="@TS_LOG_008"` — 1 scenario, 6 steps, passed, no
  HTTPS step present; full suite `mvn test` — 13 scenarios (12 passed, 1 failed), 46
  steps (45 passed, 1 failed), with the only failure being the pre-existing
  `@TS_LOG_009` loading-indicator issue (unchanged, not touched by this task). Manual
  diff confirmed a pure relocation (one step removed, one scenario added, no
  duplication or loss) and `@TS_LOG_001` byte-for-byte unchanged.
- **Priority:** Medium
- **FR/NFR:** NFR-002 (cited by the existing `# NFR-002` code-comment convention only —
  **no artifact defines `NFR-002`**, see `DR-024`); protects `FR-001`'s 1:1
  scenario-to-requirement mapping
- **Architecture section:** § Traceability (the `NFR-002` row, `@TS_LOG_008` and
  `@TS_LOG_001` with `# NFR-002` comments); § Security Considerations (Transport —
  preventive vs executed layers); § Risks and Mitigations (row: "`@TS_LOG_008` couples
  `FR-001` to the out-of-scope `NFR-002` HTTPS step")
- **Depends on:** TASK-013 (both edit the `@TS_LOG_008` scenario block in
  `login.feature`; sequencing them avoids an avoidable conflict and keeps each change
  independently reviewable)
- **Blocked by:** None. `DR-024` (how `NFR-002` should be formally handled) is an open
  **user** decision, but this task is deliberately scoped not to depend on its outcome:
  it relocates an existing assertion and carries the `# NFR-002` comment convention
  already in use. It must **not** introduce an `NFR-002` statement, measurable target,
  or acceptance criterion — only Step 1 may define one.
- **Target agent:** Implementation Agent
- **Description:** `@TS_LOG_008` — the only scenario `FR-001` traces to — ends with
  `And the current page should be served over HTTPS`, an `NFR-002` assertion that
  `artifacts/requirements.md` § Out of Scope explicitly removes from `FR-001`'s scope. A
  red `@TS_LOG_008` is therefore ambiguous about which requirement regressed (`DR-020`).
  Relocate, do not delete:
  1. Remove the trailing HTTPS step and its `# NFR-002` comment from `@TS_LOG_008`.
  2. Add a new scenario tagged `@TS_LOG_013` (the next free tag — `@TS_LOG_001`–
     `@TS_LOG_012` are all in use) containing that single `Then` step, with a
     `# NFR-002` comment recording that this is the pre-submit check on the login page.
     It inherits the same `Background` (`Given the user is on the OrangeHRM login page`),
     so the assertion runs at exactly the same point in exactly the same state — coverage
     is split, never reduced. Tag it consistently with the file's existing convention
     (`@TS_LOG_013 @security` alongside the `@TS_LOG_###` tag).
  3. Reuse the existing `the_current_page_should_be_served_over_https()` binding
     unchanged. No step-definition or page-object change is needed.
  4. **Leave the `@TS_LOG_001` occurrence of the same check completely untouched** — its
     post-submit position has distinct defence-in-depth value that the architecture
     documents deliberately (`AD-009`), and the preventive
     `ConfigReader.getBaseUrl()` guard is unaffected by this split.
- **Expected output:** Modified `src/test/resources/features/login.feature` only
  (one step removed from `@TS_LOG_008`, one new `@TS_LOG_013` scenario added,
  `@TS_LOG_001` unchanged).
- **Validation:** `mvn test -Dcucumber.filter.tags="@TS_LOG_013"` runs exactly one
  scenario and passes. `mvn test -Dcucumber.filter.tags="@TS_LOG_008"` passes and its
  `pretty` output no longer contains the HTTPS step. `mvn test` reports **thirteen**
  scenarios, all passing, confirming a split rather than a net loss. Confirm by
  inspection that `@TS_LOG_001` still carries its own HTTPS step.

### TASK-015: Review the FR-001 assertion changes against the code-quality and test rules

- **Priority:** High
- **FR/NFR:** FR-001 (AC-001); NFR-002 (via `TASK-014`)
- **Architecture section:** § SDLC Pipeline Components (Step 6 Review Agent,
  `.claude/rules/code-quality.md`, `.claude/rules/tests.md`)
- **Depends on:** TASK-012, TASK-013, TASK-014
- **Blocked by:** None
- **Target agent:** Review Agent
- **Description:** Review the full `TASK-012`–`TASK-014` diff. Specific things this
  change set can plausibly get wrong, in priority order: (a) an assertion weakened or a
  check silently dropped instead of tightened — forbidden by `.claude/rules/tests.md`;
  (b) the 15 s `explicit.wait` leaking into the 3 s missing-controls group, inflating the
  worst case past ~21 s; (c) a Selenium call appearing in `LoginSteps`, or an assertion
  appearing in `LoginPage`, breaking the layer split; (d) an expected label hard-coded in
  Java rather than supplied from the feature file; (e) a new or changed step left unbound
  (Cucumber reports `undefined`, not a failure); (f) `@TS_LOG_001`'s HTTPS step modified
  or `@TS_LOG_008` left still asserting `NFR-002`; (g) a missing `// FR-001` /
  `# NFR-002` trace comment on new steps; (h) any retired ID (`TASK-001`–`TASK-011`,
  `AD-001`–`AD-009`, `DR-001`–`DR-017`) reused, renumbered, or edited in `src/`.
- **Expected output:** Review findings with dispositions, plus any resulting fix commits
  on the same branch.
- **Validation:** Every finding is resolved or explicitly accepted with a rationale;
  `mvn test` passes after any fix; no finding is closed by relaxing an assertion.

### TASK-016: Verify FR-001 end to end and record the verification report

- **Priority:** High
- **FR/NFR:** FR-001 (AC-001); NFR-002 (`@TS_LOG_013`, traceable only via the
  `# NFR-002` comment convention pending `DR-024`)
- **Architecture section:** § Deployment (build and run commands, report outputs);
  § Reliability and Observability (reports, failure screenshots); § Traceability
- **Depends on:** TASK-015
- **Blocked by:** None
- **Target agent:** Verify Agent
- **Description:** Execute the suite and record the evidence. Run the full suite
  (`mvn test`) plus the targeted `@TS_LOG_008` and `@TS_LOG_013` runs, and confirm:
  `AC-001`'s six clauses each map to a passing assertion, including the two newly
  strict ones; thirteen scenarios execute; `ConfigReaderTest` still passes (the only
  unit test, covering `ConfigReader`'s `NFR-002` guards). Attach the real run output
  and the `target/cucumber-reports/` artifacts — never a claimed result. Record
  honestly that `NFR-002` cannot be verified against a formal requirement statement
  because none exists (`DR-024`, open), and that execution is local and on-demand with
  no CI signal (`DR-022`, open).
- **Expected output:** `artifacts/verification-report.md` with per-requirement
  coverage, the actual test output, and an explicit list of untested or unverifiable
  paths.
- **Validation:** Report shows `FR-001` → `@TS_LOG_008` → five `// FR-001`-tagged step
  methods all green, with the pasted run output as proof; every gap is stated rather
  than omitted.

### TASK-017: Sync architecture.md with the landed assertion changes

- **Priority:** Low
- **FR/NFR:** FR-001 (AC-001); NFR-002
- **Architecture section:** § Assumptions and Constraints; § Risks and Mitigations;
  § Traceability
- **Depends on:** TASK-012, TASK-013, TASK-014 (content); sequence after TASK-016 so it
  can record the verified outcome
- **Blocked by:** None
- **Target agent:** Implementation Agent
- **Description:** Three statements in `artifacts/architecture.md` are written in the
  future tense pending this plan and go stale the moment `TASK-012`–`TASK-014` land:
  the § Assumptions bullet "the assertion is to tighten to a minimum visible count in
  Step 4 — until that task lands, the one-link check is a known coverage gap"; the
  § Risks row whose mitigation reads "the step moves to its own `@TS_LOG_013` scenario in
  Step 4 … Until then, step-level reporting still identifies which step failed"; and the
  § Risks row for `DR-023`. Rewrite each to past tense citing the resolving `TASK-###`,
  and add `@TS_LOG_013` to the § Traceability `NFR-002` row's Specification column.
  **Documentation only** — no `src/`, `pom.xml`, `.claude/`, `.mcp.json`, `.gitignore`,
  or `ORCHESTRATION_LOG.md` change, no new finding, no new requirement, and no change to
  any ruling. `architecture.md` is a human-approved artifact, so present the exact diff
  for approval before applying it.
- **Expected output:** Modified `artifacts/architecture.md` (three statements and one
  table cell).
- **Validation:** No remaining "in Step 4" / "until that task lands" phrasing for
  `DR-019`, `DR-020`, or `DR-023`; every edited statement cites the `TASK-###` that
  resolved it; `git diff` touches `artifacts/architecture.md` and nothing else.

### TASK-018: Open the pull request with traceability and test evidence

- **Priority:** Medium
- **FR/NFR:** FR-001 (AC-001); NFR-002
- **Architecture section:** § SDLC Pipeline Components (Step 8 PR Agent, `github` MCP
  server); § Traceability
- **Depends on:** TASK-016, TASK-017
- **Blocked by:** None
- **Target agent:** PR Agent
- **Description:** Package the change set into a PR whose description carries the real
  diff, the real `mvn test` output from `TASK-016`, and a traceability matrix
  (`FR-001` → architecture component → `TASK-012`/`TASK-013` → code → `@TS_LOG_008`;
  `NFR-002` → `ConfigReader` guard + `TASK-014` → `@TS_LOG_013` / `@TS_LOG_001`). Carry
  forward every open and deferred item as a Known Limitation, explicitly: `DR-022` (no
  CI workflow, so this PR carries **no automated check**), `DR-024` (`NFR-002` is cited
  throughout the code but defined in no artifact), and the `Not Found` provenance of
  `AD-001`–`AD-009` / `DR-001`–`DR-017` / `TASK-001`–`TASK-011` (`DR-018`). Add the
  `artifacts/CHANGELOG.md` entry.
- **Expected output:** Pull request plus `artifacts/CHANGELOG.md` entry.
- **Validation:** Every PR section is filled from real evidence with no invented result;
  the reviewer checklist items are objectively checkable; the Known Limitations list
  omits none of the open items above.

## Parallelization Opportunities

- **`TASK-012` and `TASK-013` are logically independent** — different findings
  (`DR-019` vs `DR-023`), different assertions, no shared prerequisite — and could run
  concurrently if two implementation agents worked in separate working copies. **With a
  single Implementation Agent, run them sequentially:** both modify
  `src/test/java/com/orangehrm/pages/LoginPage.java`, and `TASK-013` additionally
  modifies `LoginSteps.java` and `login.feature`. Serialising avoids conflicts for no
  real cost — each task is a small, independently verifiable change.
- **`TASK-014` must follow `TASK-013`**, not run beside it: both edit the `@TS_LOG_008`
  scenario block (`TASK-013` adds two `And` steps, `TASK-014` removes the trailing HTTPS
  step). This is the only hard file-level ordering constraint in the plan.
- **Validation can be batched.** `TASK-015` (review) and `TASK-016` (verification) each
  cover all three code tasks in one pass, so there is no benefit to interleaving a
  review or a full suite run between `TASK-012`, `TASK-013`, and `TASK-014` — though the
  targeted per-task tag runs in each task's Validation section should still be executed
  as each task completes, to localise a failure to the task that caused it.
- **Nothing in Epic A or Epic B can be parallelised with `TASK-017` or `TASK-018`**:
  both consume the landed, verified result.
- Note: no suite-level parallelism is available (JUnit 4 runner, no Surefire `parallel`
  / `forkCount`) — "parallel" here means concurrent *agent work*, not concurrent
  scenario execution.

## Blocked Tasks

| Task | Blocked by | Unblock criteria |
| --- | --- | --- |
| None | — | No task in this plan is blocked. All three code tasks (`TASK-012`, `TASK-013`, `TASK-014`) are scoped to be startable immediately after human approval of this plan. |

`TASK-014` is the one task that **could** have been blocked, and deliberately is not:
`DR-024` (how `NFR-002` should be formally handled) is an unresolved user decision, but
the task relocates an existing assertion and relies only on the `# NFR-002` code-comment
convention already in use, so it needs no `NFR-002` definition to proceed. If the user
later chooses `DR-024` option (i) — amend `requirements.md` with a measurable `NFR-002`
via a Step 1 revision — a **future** task (`TASK-019`+) would retrofit the formal
`NFR-###` trace onto `@TS_LOG_013`, `ConfigReader`, and `ConfigReaderTest`. That task is
intentionally **not** planned here: planning it would require assuming a definition this
plan is not permitted to invent.

## Risks and Open Questions

**Risks carried into implementation:**

| Risk | Mitigation |
| --- | --- |
| The live demo footer renders fewer social links than `TASK-012`'s floor, making the suite fail with no product defect | Confirm the live count against the DOM before finalising the constant; keep the floor at the requirement-derived minimum (`2`) rather than the observed count, so a cosmetic footer change does not break the build |
| `TASK-012` accidentally inflates the missing-controls worst case from ~21 s to ~33 s by reusing the 15 s `explicit.wait` | Explicit-timeout helper is mandated in the task; `TASK-015` review item (b) checks for it specifically |
| A new or renamed Gherkin step silently unbinds (Cucumber reports `undefined`, which can read as a pass at a glance) | Each task's Validation requires inspecting the Cucumber summary for `undefined` steps, not just the exit code |
| Demo-site markup change or outage fails `TASK-012`/`TASK-013` validation with no product defect | Accepted and documented (`DR-027`): all `FR-001` locators sit in `LoginPage`, so a markup fix is one file; the failure screenshot plus the missing-control list distinguishes "site down" from "control missing" |
| `TASK-014` is misread as permission to delete the HTTPS check | The task text, its Expected output, and its Validation all state the scenario count must rise to thirteen and `@TS_LOG_001` must stay untouched |
| `TASK-017` edits a human-approved artifact | Documentation-only, scoped to three stale statements and one table cell, and requires an approved diff before applying |

**Open questions — both require a user decision; neither blocks this plan:**

- **`DR-024` — how should `NFR-002` be handled?** It is asserted in `login.feature`,
  guarded in `ConfigReader`, unit-tested in `ConfigReaderTest`, and mapped in
  `architecture.md`, yet no artifact defines it and no `NFR-001` exists. Choose (i) amend
  `requirements.md` via a Step 1 revision with a measurable `NFR-002`, or (ii) record
  `NFR-002` formally as inherited from unavailable prior work and out of scope for this
  run, accepting that `TASK-016` cannot verify it against a requirement statement. This
  plan does not choose, and does not invent a definition.
- **`DR-022` — is local, on-demand verification the accepted operating model?** No
  workflow builds or runs this suite, so there is no push/PR regression signal and no
  report retention, and `TASK-018`'s PR will carry no automated check. Confirm this is
  accepted for the capstone, or schedule a CI workflow as separate, post-`FR-001` work.
  **No CI task is planned here** — `DR-022` is deferred and escalated, not accepted
  in-scope work.

**Settled in Step 3 and deliberately not re-opened by this plan:** the ID baseline
(`DR-018`), the social-link plural (`DR-019`), the `NFR-002` step split (`DR-020`), the
footer-container ruling (`DR-021`), and the five closed `Low` findings (`DR-021`,
`DR-025`, `DR-027`, `DR-028`, `DR-029`).

## Recommended First Task for Implementation Agent

`TASK-012` — Tighten the footer social-link assertion to a minimum visible count.

It is the highest-priority task, depends on nothing, is blocked by nothing, and closes
the coverage gap that `architecture.md` § Assumptions currently records as live
(`DR-019`). It also discharges `DR-026` by giving `BasePage`'s only uncalled wait helper
a real caller or a replacement, which every later task benefits from. Scope is two files
and one new constant.
