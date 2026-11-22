# Design Review

## Review Summary

**Overall assessment: the architecture is sound and is approved for planning.** No
`Critical` and no `High` findings were raised. Every claim in
`artifacts/architecture.md` that this review could check against the repository was
verified as accurate — the four-layer split (Specification → Glue → Page Objects →
Infrastructure) holds in the code, the glue layer genuinely contains zero Selenium
calls, `FR-001` is realized by exactly one scenario (`@TS_LOG_008`) with three
`// FR-001`-tagged step methods, and both Mermaid diagrams match the implementation
(including the Background's `ConfigReader.getBaseUrl()` → `LoginPage.open()` path and
the seven independent 3 s visibility checks in `findMissingLoginControls()`). The
Architect's decision to raise discrepancies as Open Questions rather than resolve them
by assumption was the correct call and made this review tractable.

Three themes account for all twelve findings:

1. **Assertion strictness, not assertion absence (`DR-019`, `DR-023`).** `FR-001` is
   covered end-to-end, but two of its clauses are asserted more loosely than the
   requirement states: "social link**s**" (plural) is satisfied by one visible link,
   and the literal control labels `"Login"` and `"Forgot your password?"` — quoted
   verbatim in `FR-001` — are never compared, only located structurally. These are
   partial-regression blind spots. They are the substance of the work Step 4 should
   plan, which is precisely why they are `Medium` and not blockers.
2. **Traceability hygiene (`DR-018`, `DR-020`, `DR-024`).** The repository inherits
   `AD-###` / `DR-###` / `TASK-###` citations whose defining documents are absent, and
   cites an `NFR-002` that no artifact ever defines. Neither damages `FR-001`'s own
   chain, which is complete, but both must be ruled on before Step 4 mints new IDs.
3. **Deliberate, documented scope limits (`DR-021`, `DR-022`, `DR-025`, `DR-027`,
   `DR-028`).** No CI, no stub/offline path, no retries, a committed public demo
   password, and a `LoginPage` that also serves out-of-scope scenarios. Each was
   examined on its merits and each is accepted — they are consequences of the capstone's
   single-`FR` scope and of a read-only external system under test, not oversights.

**Readiness: Ready for planning.** The five `Medium` findings are explicitly accepted
and routed (three to Step 4 as tasks, two to a user decision) rather than waved
through; none of them blocks the Planner Agent.

## Review Inputs

- `artifacts/architecture.md` — commit `b448cb4` ("docs: define architecture for
  US-AUTH-001 login page controls (FR-001)"), human-approved, reviewed in full.
- `artifacts/requirements.md` — commit `3acb3f5`, human-approved and unchanged;
  `FR-001`, `AC-001`, and the Out of Scope list used as the alignment baseline.
- `.claude/skills/design-review/SKILL.md` (eleven review areas, severity
  definitions, document structure) and `.claude/skills/sdlc-traceability/SKILL.md`
  (ID conventions, never-renumber rule).
- `.claude/rules/sdlc-artifacts.md`, `.claude/rules/code-quality.md`,
  `.claude/rules/tests.md`.
- Repository evidence consulted to validate (not to re-review) the architecture:
  `src/test/resources/features/login.feature`, `pages/LoginPage.java`,
  `pages/BasePage.java`, `stepdefinitions/LoginSteps.java`, `hooks/Hooks.java`,
  `runner/TestRunner.java`, `config/ConfigReader*.java`,
  `src/test/resources/config.properties`, `pom.xml`, `.github/**`, `git log`.
  No source file was modified by this review.

## Findings

Finding IDs start at `DR-018`. See `DR-018` for why this review does **not** start at
`DR-004`.

| ID | Severity | Area | Finding | Impact | Recommendation | Decision | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `DR-018` | Medium | SDLC pipeline coverage | Code and `pom.xml` cite `AD-008`, `AD-009`, `DR-001`, `DR-002`, `DR-003`, `DR-017`, `TASK-001/002/003/011`; no artifact in `artifacts/` defines any of them (architecture.md § Traceability records all ten as `Not Found`). The highest cited IDs are `AD-009`, `DR-017`, `TASK-011`. | Step 4 minting `TASK-001` or this review minting `DR-001` would collide with a live code citation and silently corrupt the chain in both directions. `DR-004`–`DR-016` are uncited but must be presumed consumed, since a prior document numbered as far as `DR-017`. | Adopt commit `b448cb4` as the new numbering baseline. Allocate strictly above every cited ID: design-review findings from **`DR-018`**, architecture decisions from **`AD-010`**, tasks from **`TASK-012`**. Never reuse, renumber, or reinterpret `AD-001`–`AD-009`, `DR-001`–`DR-017`, `TASK-001`–`TASK-011`; leave their code comments untouched and keep them listed as `Not Found` provenance. | **Accepted — option (a), continue numbering above the cited range.** Reconstructing the prior documents is rejected (would invent content, violating `.claude/rules/sdlc-artifacts.md`'s "`Not Found` over inventing"); renumbering the code comments is rejected (violates the never-renumber rule and would churn `src/`, which is out of scope for Step 3). Next free IDs after this review: `AD-010`, `DR-030`, `TASK-012`. | Resolved |
| `DR-019` | Medium | Requirements alignment / Testability | `FR-001` and `AC-001` say "social link**s**" (plural), but `LoginPage.findMissingLoginControls()` checks `isDisplayed(FOOTER_SOCIAL_LINK, 3)` on `.orangehrm-login-footer .orangehrm-login-footer-sm a`, which passes on the **first** match. A regression removing all but one social link satisfies `@TS_LOG_008`. | A real, demonstrable coverage gap against the approved wording of `AC-001`: a partially-collapsed footer is reported as a pass, and the defect reaches Step 7/Step 8 unseen. | Raise a `TASK-###` for Step 4: assert a minimum visible social-link count (≥ 2 as the requirement-derived floor for the plural; confirm the exact live count against the DOM during Step 5 rather than hard-coding an unverified number). Keep the count in a named constant in `LoginPage`, keep the missing-control name in the failure list, and honour the 3 s group budget — `BasePage.waitForAllVisible` uses the 15 s `explicit.wait`, so reusing it as-is would raise the all-missing worst case from ~21 s to ~33 s and contradict the `DR-003` rationale. Prefer a count-aware helper with an explicit timeout. | **Accepted as a finding — not an accepted gap.** The plural is explicit in approved requirement text, and the fix is small, local to `LoginPage` plus one constant, and carries no scope increase. Routed to Step 4. | Accepted — route to Step 4 |
| `DR-020` | Medium | Requirements alignment | `@TS_LOG_008` ends with `And the current page should be served over HTTPS`, an `NFR-002` assertion that `artifacts/requirements.md` § Out of Scope explicitly removes from `FR-001`'s scope. The scenario `FR-001` traces to therefore fails on an `NFR-002`-only regression. | Scenario-to-requirement mapping stops being 1:1, so a red `@TS_LOG_008` is ambiguous about which requirement regressed, and Step 7's verification report will attribute an HTTPS regression to `FR-001`. Conversely, `FR-001` cannot be verified green in an environment where only the transport check fails. | Raise a `TASK-###` for Step 4: move the HTTPS step out of `@TS_LOG_008` into its own scenario (next free tag `@TS_LOG_013`) carrying a `# NFR-002` comment. It inherits the same `Background`, so the pre-submit coverage is preserved exactly — this is a split, not a deletion. Leave the `@TS_LOG_001` occurrence untouched: its post-submit position has distinct defence-in-depth value that the architecture correctly documents. Sequence after or with `DR-024`, so the new scenario has a defined requirement to trace to. | **Accepted as a design finding — bundling rejected.** Defence-in-depth is a valid argument for *having* the assertion, not for hosting it inside a scenario whose requirement explicitly excludes it; the real preventive control is `ConfigReader.getBaseUrl()`, which is unaffected by the split. One scenario per requirement is the property the pipeline's traceability depends on. Routed to Step 4. | Accepted — route to Step 4 |
| `DR-021` | Low | Requirements alignment | No locator targets the `.orangehrm-login-footer` container; `AC-001`'s "footer … visible" is inferred from two visible descendants (`.orangehrm-copyright-wrapper`, `.orangehrm-login-footer-sm a`). | None in practice. Both locators are descendant-scoped to `.orangehrm-login-footer`, so a match is impossible unless the container is in the DOM, and `visibilityOfElementLocated` on a descendant cannot succeed while an ancestor is `display:none` or zero-sized. | No change to the assertions. Promote this from an unverifiable "assumption" to a confirmed design decision with the rationale recorded in `architecture.md`, so a later agent does not re-litigate it or add a redundant eighth check. | **Sufficient as-is — accepted, no code change.** Descendant visibility is strictly *stronger* evidence than asserting the container: a visible-but-empty `div.orangehrm-login-footer` would satisfy a direct container check while failing `AC-001`'s intent. Adding an eighth control would cost up to 3 s more worst case and add zero detection power. | Closed — accepted as-is |
| `DR-022` | Low | Deployment and CI/CD | No workflow builds or runs this suite. `.github/` contains only `modernize/java-upgrade` scaffolding (three hook scripts, no `workflows/` directory) — confirmed by inspection. | `FR-001` is verified only on demand on a developer machine; there is no regression signal on push/PR, and no retention of the Cucumber HTML/JSON/JUnit-XML reports or failure screenshots beyond the local `target/` directory. Step 8 will open a PR with no automated check attached to it. | Record the gap and keep it visible; do not add a workflow in this pipeline run. The framework is already CI-ready (`headless=true`, `-D`/env secret injection, JUnit-XML output), so this remains a small self-contained change whenever it is wanted. Escalated to the user as an Open Question for a future run. | **In scope to flag, out of scope to fix.** Neither `FR-001` nor `AC-001` asks for continuous or automated execution, and Step 7 (Verify Agent) supplies this run's execution evidence. Flagged — not silently dismissed as `Not Found` — so no downstream agent assumes CI exists. | Deferred — escalated to user |
| `DR-023` | Medium | Requirements alignment / Testability | `FR-001` quotes two literal labels — a `"Login"` submit button and a `"Forgot your password?"` link — but both are located purely structurally (`button[type='submit']`, `.orangehrm-login-forgot-header`) and their text is never asserted. Only the two placeholder values have text assertions. Not raised in `architecture.md`. | Symmetrical to `DR-019` and arguably more direct, since the requirement states the strings verbatim: a mislabelled or relabelled control ("Sign in", "Reset password") passes `@TS_LOG_008`. The structural `button[type='submit']` locator would also match a different submit button if the form gained one. | Raise a `TASK-###` for Step 4: add `getLoginButtonText()` / `getForgotPasswordLinkText()` readers to `LoginPage` and two `Then` steps asserting the expected labels, following the existing placeholder-step pattern (page object reads, step layer asserts). Compare on trimmed/whitespace-normalised text to avoid DOM-whitespace flakiness. | **Accepted as a finding.** Same class of gap as `DR-019`, same small and local fix, and it closes the last loose clause of `FR-001`. Routed to Step 4; the two tasks are natural siblings. | Accepted — route to Step 4 |
| `DR-024` | Medium | SDLC pipeline coverage | `NFR-002` is cited in `architecture.md` (drivers table, Security, Traceability), in `login.feature` (two `# NFR-002` comments), and in `ConfigReader`, `ConfigReaderTest`, and `LoginSteps` — but **no artifact defines it**. `requirements.md` mentions `NFR-002` only inside § Out of Scope, as a pointer to work "tracked separately"; there is no `NFR-002` statement, measurable target, or acceptance criterion anywhere, and no `NFR-001` exists. | An undefined NFR cannot be verified by Step 7 and cannot be meaningfully regression-tested; `.claude/rules/sdlc-artifacts.md` requires measurable targets for non-functional requirements. It also leaves `DR-020`'s new scenario with no requirement to trace to, and leaves `ConfigReaderTest` — the only unit test — tracing to an ID with no definition. | Either (i) amend `requirements.md` in a follow-up Step 1 revision with a proper `### NFR-002` (all traffic to `base.url` served over `https`; preventive scheme guard before navigation; executed scheme re-check; audited single opt-out), or (ii) record explicitly that `NFR-002` is inherited from prior, unavailable work and out of scope for this run. Do **not** invent a definition inside `architecture.md` or this document. | **Accepted as a finding; escalated to the user.** `requirements.md` is approved and unchanged and Step 3 must not rewrite it, so the choice between (i) and (ii) belongs to the user. Does not block `FR-001`, whose own chain (PRD → `FR-001`/`AC-001` → `LoginPage` → `@TS_LOG_008` → three tagged steps) is complete and unbroken. | Accepted — escalated to user |
| `DR-025` | Low | Components and responsibilities | `LoginPage` owns `FR-001`'s locators *and* readers for four out-of-scope concerns (error banner, inline validation, password-type masking, loading indicator); `LoginSteps` instantiates `DashboardPage` and `ResetPasswordPage` for every scenario, including `@TS_LOG_008`, which needs neither. The architecture already names this drift in its Technology Choices trade-offs. | Mild single-responsibility erosion. No functional impact: the objects are cheap, hold no state, and `FR-001`'s DOM knowledge is still confined to one file, which is the property that matters for locator maintenance. | No change. Revisit only if `LoginPage` grows past the login page's own concerns or if a second page's locators appear in it. | **Accepted as-is.** Splitting a 12-scenario suite's single page object by acceptance criterion would add indirection and cross-file locator hunting for no maintenance gain, and would touch out-of-scope code. | Closed — accepted as-is |
| `DR-026` | Low | Testability and maintainability | `BasePage.waitForAllVisible(By)` is declared but has **zero** callers anywhere in `src/` (verified). `architecture.md` cites it as "already exists if a count assertion is wanted", which is true but currently dead code. | Unused code drifts and misleads: it reads as a supported, exercised helper when nothing proves it works, and it silently uses the 15 s `explicit.wait` rather than any per-check budget. | Do not delete it in isolation. Fold it into `DR-019`'s task: either give it first real use with an explicit-timeout overload, or replace it with the count-aware helper that task needs. Whichever is chosen, it ends the task with a caller. | **Accepted, folded into `DR-019`.** Resolving the two together avoids a churn-only commit and avoids deleting a helper the next task would immediately re-add. | Accepted — folded into `DR-019` |
| `DR-027` | Low | Reliability and fault tolerance | `@TS_LOG_008` depends entirely on a reachable third-party demo site: no stub, no recorded-DOM fallback, no retry or rerun mechanism, and locators keyed on a vendor's hand-authored CSS classes. | A demo-site outage, rate-limit, or markup change fails the suite with no product defect, and there is no way to verify `FR-001` offline. The architecture rates this the single largest `FR-001` fragility and this review agrees. | No change. Existing mitigations are proportionate: 30 s page-load / 15 s explicit timeouts fail fast with clear messages, the missing-control list plus failure screenshot distinguishes "site down" from "control missing", and all `FR-001` locators sit in one file so a markup fix is a one-file change. | **Accepted as documented.** Unavoidable given a requirement whose system under test is an external site this repository does not own. The deliberate absence of retries is correct per `.claude/rules/tests.md` determinism — a flaky `FR-001` failure must stay visible rather than be retried away. | Closed — accepted as documented |
| `DR-028` | Low | Security and access control | `src/test/resources/config.properties` commits `valid.username=Admin` / `valid.password=admin123`. | Low: these are OrangeHRM's **public** demo credentials for a public demo instance, and `ConfigReader.getValidPassword()` enforces the scope in code by refusing the committed value when `base.url` leaves `opensource-demo.orangehrmlive.com`, demanding `-Dvalid.password` or `VALID_PASSWORD` instead. `FR-001`/`@TS_LOG_008` uses no credentials at all, so this is not on the reviewed path. | No change. Keep the host restriction and the `-D` → env → file precedence; `.claude/hooks/pre_implementation.md` already requires confirming `base.url` and checking for new hard-coded secrets before any Step 5 change. | **Accepted as documented** — a deliberate, code-enforced, scoped exception rather than a leaked secret. Standing condition: if `base.url` is ever repointed, the guard must keep forcing injected credentials. | Closed — accepted as documented |
| `DR-029` | Low | Scalability and performance | `.claude/CLAUDE.md` states the `ThreadLocal` driver means "the suite can run in parallel (Surefire forking / parallel Cucumber runners)". In reality `pom.xml` sets no `parallel`/`forkCount` and `@RunWith(Cucumber.class)` (JUnit 4 + `cucumber-junit`) cannot execute scenarios in parallel. `architecture.md` § Scalability states this correctly. | A downstream agent trusting `CLAUDE.md` could assume parallel-safe execution already works and plan around throughput that does not exist. | No action in Step 3 — `.claude/` is outside this step's change scope. `architecture.md` is the authoritative statement; treat parallelism as **prepared but not enabled**, requiring a JUnit 5 / `cucumber-junit-platform-engine` migration that is out of scope for `FR-001`. | **Noted, no change.** Recorded so the discrepancy is on the record for whoever next edits `CLAUDE.md`. | Noted — no action this step |

**Counts:** `Critical` 0 · `High` 0 · `Medium` 5 (`DR-018`, `DR-019`, `DR-020`,
`DR-023`, `DR-024`) · `Low` 7 (`DR-021`, `DR-022`, `DR-025`, `DR-026`, `DR-027`,
`DR-028`, `DR-029`).

## Agreed Design Decisions

| Decision | Rationale | Owner |
| --- | --- | --- |
| Commit `b448cb4`'s `architecture.md` is the numbering baseline. New IDs are allocated strictly above every ID cited in code: `AD-010`+, `DR-018`+, `TASK-012`+. `AD-001`–`AD-009`, `DR-001`–`DR-017`, `TASK-001`–`TASK-011` are permanently retired as `Not Found` and are never reused, renumbered, or reinterpreted. | Only an allocation above the whole cited range is collision-free, because a prior document demonstrably numbered to at least `DR-017` even though `DR-004`–`DR-016` are uncited. Honours the never-renumber rule in `sdlc-traceability` and keeps `src/` untouched. (`DR-018`) | Design Review Agent (decided); Planner Agent (applies from `TASK-012`) |
| `FR-001`'s "social links" requires evidence of **more than one** link; the assertion tightens to a minimum visible count held in a named constant, within the existing 3 s per-control budget. | The plural is explicit in approved requirement text; a one-link pass is a real partial-regression blind spot. Fix is local to `LoginPage` and one new constant. (`DR-019`, with `DR-026`) | Planner Agent (`TASK-012`+), then Implementation Agent |
| The `NFR-002` HTTPS assertion moves out of `@TS_LOG_008` into its own scenario (`@TS_LOG_013`), inheriting the same `Background`; the `@TS_LOG_001` occurrence stays. | Restores one-scenario-per-requirement so a red `@TS_LOG_008` means exactly "`FR-001` regressed". Coverage is split, never reduced, and the preventive `ConfigReader` guard is untouched. (`DR-020`) | Planner Agent, then Implementation Agent |
| `FR-001`'s quoted labels `"Login"` and `"Forgot your password?"` get explicit, whitespace-normalised text assertions via new `LoginPage` readers and `Then` steps. | The requirement states the strings verbatim; structural locators alone cannot detect a relabelled control. Mirrors the existing placeholder-assertion pattern. (`DR-023`) | Planner Agent, then Implementation Agent |
| `AC-001`'s "footer … visible" is **satisfied** by the two descendant-scoped visibility checks. No footer-container locator is added. | Descendant visibility implies the container is present and rendered, and is stronger than a container check, which an empty footer would pass. Recorded as a confirmed decision so it is not re-litigated. (`DR-021`) | Design Review Agent (closed) |
| Local, on-demand execution is the accepted operating model for this capstone run; no CI workflow is added now, and the gap stays on the record. | `FR-001`/`AC-001` do not require automated execution and Step 7 provides execution evidence; the framework stays CI-ready for a later, self-contained change. (`DR-022`) | User (confirm), Design Review Agent (recorded) |
| The committed public demo credentials, the absent stub/retry path, and `LoginPage`'s out-of-scope readers are accepted as documented trade-offs, each with its stated standing condition. | Each is proportionate to a read-only, externally hosted system under test and a 12-scenario suite; none is on `FR-001`'s verification path in a way that weakens it. (`DR-025`, `DR-027`, `DR-028`) | Design Review Agent (closed) |
| Parallel execution is treated as **prepared but not enabled** until a JUnit 5 migration; `architecture.md` § Scalability overrides `CLAUDE.md`'s broader claim. | Prevents downstream throughput assumptions that the JUnit 4 runner cannot honour. (`DR-029`) | Design Review Agent (recorded) |

## Rejected or Deferred Findings

| ID | Finding | Rationale |
| --- | --- | --- |
| `DR-018` (alt. b) | Reconstruct the missing `architecture.md` / `design-review.md` / `impl-plan.md` that defined `AD-001`–`AD-009`, `DR-001`–`DR-017`, `TASK-001`–`TASK-011`. | **Rejected.** Their original content is unknowable from this repository; writing it would be invention, which `.claude/rules/sdlc-artifacts.md` forbids ("`Not Found` rather than inventing content"). The inferred-meaning table already in `architecture.md` § Traceability preserves everything that *can* be evidenced. |
| `DR-018` (alt. c) | Renumber the `AD-###`/`DR-###`/`TASK-###` citations in `src/` and `pom.xml` onto the new baseline. | **Rejected.** Violates the never-renumber rule, destroys the only surviving record of the prior chain, and would modify `src/`/`pom.xml`, which Step 3 must not touch. |
| `DR-020` (alt.) | Keep the HTTPS step inside `@TS_LOG_008` as pragmatic defence-in-depth, since the page is already open. | **Rejected.** Cheapness of the assertion is not a reason to bind an out-of-scope requirement to `FR-001`'s only scenario. The split keeps the same assertion on the same already-open page via the shared `Background`, so the pragmatic benefit survives at no cost. |
| `DR-021` (alt.) | Add `.orangehrm-login-footer` as an eighth asserted control. | **Rejected.** Adds up to 3 s to the worst case and no detection power; a visible empty footer would pass it while failing `AC-001`'s intent. |
| `DR-022` | Add a GitHub Actions workflow to build and run the suite. | **Deferred and escalated.** Real gap, but outside `FR-001`/`AC-001` and this run's single-`FR` scope; adding it is a scope decision for the user, not a default for this agent to choose. |
| `DR-024` | Define `NFR-002` with a measurable target and acceptance criterion. | **Escalated, not actioned here.** `requirements.md` is approved and unchanged; only Step 1 may amend it. Non-blocking for `FR-001`. |
| `DR-026` | Remove the unused `BasePage.waitForAllVisible` helper. | **Deferred into `DR-019`.** Deleting it now would mean re-adding it in the very next task; `DR-019` gives it a real caller instead. |
| `DR-029` | Correct `.claude/CLAUDE.md`'s parallel-execution claim. | **Deferred.** `.claude/` is outside Step 3's change scope; `architecture.md` already carries the accurate statement. |

## Required Architecture Updates

Applied to `artifacts/architecture.md` in this step (documentation only — no `src/`,
`pom.xml`, `.claude/`, `.mcp.json`, or `.gitignore` change):

- **§ Open Questions** — replaced the five unresolved questions with the five rulings,
  each citing its `DR-###` and its disposition, so the next agent does not re-ask
  settled questions. The two items that remain genuinely open (`DR-022` CI, `DR-024`
  `NFR-002` definition) are restated as **user decisions**, not as architect questions.
- **§ Traceability** — added the `DR-018` ID-allocation rule (`AD-010`+ / `DR-030`+ /
  `TASK-012`+, retired ranges never reused) directly above the `Not Found` citation
  table, where the next agent will look for it.
- **§ Assumptions and Constraints** — moved the footer-inference bullet from
  *Assumptions* to *Confirmed constraints* per the `DR-021` ruling, and rewrote the
  "at least one social link" assumption to record that `DR-019` supersedes it.
- **§ Risks and Mitigations** — repointed the three rows that referenced "Open
  Question 1/2" or "raised in Open Questions" at `DR-018`, `DR-019`, and `DR-020`, and
  updated their mitigation text to the agreed decisions.
- **§ Deployment** — repointed the CI/CD `Not Found` note at `DR-022` and its ruling.

No change was required to the Overview, drivers table, component diagram, key
components table, data flow diagram, technology choices, security considerations,
reliability, or scalability sections: all were verified against the repository and
found accurate as written.

## Open Questions

Both require a user decision; neither blocks Step 4 for `FR-001`.

- **`DR-024` — how should `NFR-002` be handled?** It is asserted in the feature file,
  guarded in `ConfigReader`, unit-tested in `ConfigReaderTest`, and mapped in
  `architecture.md`, yet no artifact defines it and no `NFR-001` exists. Choose:
  (i) amend `requirements.md` via a Step 1 revision with a measurable `NFR-002`
  (preferred — it makes the existing implementation verifiable and gives `DR-020`'s
  new `@TS_LOG_013` scenario a requirement to trace to), or (ii) record `NFR-002`
  formally as inherited from unavailable prior work and out of scope for this run,
  accepting that Step 7 cannot verify it. This review will not invent a definition.
- **`DR-022` — is local, on-demand verification the accepted operating model?** No
  workflow runs this suite, so there is no push/PR regression signal and no report
  retention; Step 8 will open a PR with no automated check. Confirm that this is
  accepted for the capstone, or schedule a CI workflow as a separate, post-`FR-001`
  piece of work. The framework is already CI-ready, so this is a small change whenever
  it is wanted.

Settled in this review and **not** open: the ID baseline (`DR-018`), the social-link
plural (`DR-019`), the `NFR-002` step split (`DR-020`), and the footer-container
ruling (`DR-021`).

## Readiness Recommendation

**Ready for planning.**

No `Critical` and no unresolved `High` findings. `DR-018` and `DR-021` are resolved and
closed within this review. `DR-019`, `DR-020`, and `DR-023` are accepted and carry
concrete, scoped recommendations for the Planner Agent to turn into `TASK-012`+ (with
`DR-026` folded into `DR-019`'s task). `DR-022` and `DR-024` are escalated to the user
as explicit Open Questions and are deliberately **not** recorded as assumptions.

Hand-off instructions for the **Planner Agent (Step 4)**:

1. Number tasks from **`TASK-012`** — never `TASK-001`–`TASK-011` (`DR-018`).
2. Plan three tasks, each citing `FR-001` and its architecture section: tighten the
   social-link assertion to a minimum count and give `waitForAllVisible` a real caller
   (`DR-019` + `DR-026`); split the `NFR-002` HTTPS step into `@TS_LOG_013`
   (`DR-020`); assert the `"Login"` and `"Forgot your password?"` labels (`DR-023`).
3. Sequence `DR-020`'s task after the user answers `DR-024`, or plan it so the new
   scenario's `# NFR-002` comment is the only traceability it needs if option (ii) is
   chosen.
4. Plan no work for `DR-021`, `DR-025`, `DR-027`, `DR-028`, or `DR-029` — all closed
   as accepted. Plan no CI work unless the user reopens `DR-022`.
