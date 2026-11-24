---
name: code-review
description: "Perform a structured peer code review of the implementation against artifacts/requirements.md before a PR is created. Used by the Review Agent (Step 6) to evaluate correctness, security, error handling, test coverage, clarity, DRY, and dependency safety. Gates the PR on Blocker/Major findings."
---

# Skill: Code Review
This skill is loaded by the **Review Agent** (Step 6 of the Agentic SDLC Pipeline) to perform a structured, evidence-based code review before a pull request is created.
Follow these steps exactly.

## Step 1 — Establish review context
1. Read `artifacts/requirements.md` to identify every functional and non-functional requirement (`FR-###`, `NFR-###`) and their acceptance criteria.
2. Identify the components, files, and tests that make up the current implementation. Use `git diff main...HEAD` to determine the change set.
3. Load the [[sdlc-traceability]] skill to link every finding to a file location and the requirement it affects.
4. Do not review assumptions — review only code that exists in the diff.

## Step 2 — Structured code review
Evaluate each area in the checklist below, one area at a time. For every finding, record:
- **File + location** — e.g. `.java`
- **Severity** — `Blocker`, `Major`, `Minor`, or `Nit` (see definitions below)
- **Requirement link** — the `FR-###` or `NFR-###` the finding affects, if any
- **Proposed fix** — a concrete, specific change (not a vague suggestion)

### Review checklist
| Review Area | What to check |
| --- | --- |
| **Correctness** | Trace each `FR-###`/`NFR-###` to the code that implements it. Flag missing behavior, logic errors, missing branches, and unmet acceptance criteria. |
| **Security** | Confirm secrets/tokens/credentials are never logged or returned, and all external/user-supplied input is validated (OWASP Top 10: injection, broken access control, sensitive data exposure). For auth flows: mask passwords, use HTTPS, and return generic errors that do not reveal whether the identifier or the password was wrong. |
| **Error Handling** | Confirm API failures, timeouts, missing files, and empty/invalid states are handled gracefully with no unhandled exceptions. Fail closed on security-relevant errors. |
| **Test Coverage** | Verify tests cover the happy path and edge cases (`Not Found`, missing fields, empty input, error branches). Flag any critical path with no automated test. |
| **Code Clarity** | Flag needless complexity, confusing names, and dead code. A comment is warranted only to explain *why* something non-obvious is done. |
| **DRY Principle** | Identify duplicated logic and recommend extracting it into a single, well-named shared function or module — without refactoring beyond what removing the duplication requires. |
| **Dependency Safety** | Flag known-vulnerable or outdated package versions; recommend a safe, maintained alternative or version bump. |

## Step 3 — Severity definitions
| Severity | Definition |
| --- | --- |
| **Blocker** | Incorrect behavior, data loss, or exploitable security issue. Must be fixed before any PR is created. |
| **Major** | Significant risk, unmet requirement, or missing critical test. Must be resolved before signaling PR-ready. |
| **Minor** | Worth fixing but will not block the PR. Record as open follow-up. |
| **Nit** | Style or preference only. Optional; keep as a note. |

## Step 4 — Remediate and verify
1. Propose a concrete fix for every **Blocker** and **Major** finding.
2. Apply fixes directly when the change is clearly correct and in scope.
3. After applying fixes, re-run the gated test suite:
   ```bash
   mvn test
   ```
4. Re-review changed code to confirm the finding is closed and nothing regressed.
5. Escalate ambiguous or high-impact decisions to the user — do not resolve them
   silently.

## Step 5 — Gate and report
1. Confirm there are **no unresolved Blocker or Major findings** before signaling that the implementation is PR-ready.
2. Record any accepted **Minor** or **Nit** items and any deferred work as open follow-ups.
3. Report the final outcome using one of three verdicts:
   | Verdict | Condition |
   | --- | --- |
   | ✅ **Approve** | No Blocker or Major findings remain |
   | 💬 **Approve with comments** | Only Minor / Nit findings remain |
   | 🚫 **Changes requested** | One or more Blocker or Major findings remain |

4. Hand off to the **Verify Agent** (Step 7) only on ✅ Approve or 💬 Approve with comments.