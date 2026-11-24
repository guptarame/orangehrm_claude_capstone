---
description: "Run the full Agentic SDLC Pipeline by executing the orchestrator prompt in .claude/agents/orchestrator.prompt.md."
---

# /run-pipeline
Read `.claude/agents/orchestrator.prompt.md` in full and execute its **Execution Instructions** exactly as written — Phase A (Pre-Flight Check), Phase B (Step Execution), and Phase C (Pipeline Completion) — including all human review gates, the `ORCHESTRATION_LOG.md` state tracking, and the error-handling rules defined there.
Do not duplicate or re-derive the pipeline logic here — `orchestrator.prompt.md` is the single source of truth. This command only dispatches to it.