---
name: pre-implementation
description: "Pre-flight gate the Implementation Agent (Step 5) runs before loading the implement-task skill or touching any file."
used-by: ".claude/agents/implementation.agent.md"
---

# Pre-Implementation Hook
Run this checklist **before** invoking `/implement-task` and before editing, writing, or running anything. If any item fails, stop and escalate to the user instead of working around it — do not silently skip, assume, or "fix" a failed check.

Per-task checks — task selection, blocked/dependency status, and the change-summary approval gate — are owned by the `implement-task` skill's own Steps 2–3. Do not duplicate them here; this hook only covers what must hold true before any task is
even picked.

## 1. Upstream gate
- [ ] `artifacts/impl-plan.md` exists.
- [ ] Step 4 (Planner Agent) has been **explicitly approved** by the human reviewer for this run — do not proceed on a draft or unreviewed plan.
- [ ] `artifacts/requirements.md`, `artifacts/architecture.md`, and `artifacts/design-review.md` exist and are not contradicted by `impl-plan.md`.

## 2. Workspace safety
- [ ] `git status` is checked before any change. Uncommitted work that isn't yours (from a prior session) is stashed or flagged to the user — never discarded.
- [ ] The working tree builds cleanly before you start, so any failure after your change is attributable to your change and not pre-existing breakage:
  ```powershell
  mvn clean install -DskipTests
  ```

## 3. Environment / credential check (project-specific)
- [ ] `src/test/resources/config.properties` → `base.url` targets the intended environment. If a task would point the suite at anything other than the public `opensource-demo.orangehrmlive.com` host, confirm with the user before proceeding — `ConfigReader.getValidPassword()` already refuses the committed demo password off that host, but the task scope itself must be confirmed too.
- [ ] No new secret, token, or credential is about to be hard-coded. Anything beyond the already-accepted demo values in `config.properties` must come from a `-D` system property or environment variable, never a committed literal.

Only once every box above is true, proceed to load and follow the `implement-task` skill — which then handles task selection, blocked/dependency checks, and the human approval gate before any change is applied.