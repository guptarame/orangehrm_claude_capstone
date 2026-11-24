---
description: "Authoring standards for the Agentic SDLC deliverable documents (requirements, architecture, design review, implementation plan, changelog)."
paths:
  - "artifacts/**/*.md"
  - "artifacts/ORCHESTRATION_LOG.md"
---

# SDLC Artifact Standards
These standards apply to every Markdown deliverable produced by the pipeline.

## Structure and traceability
- Follow the ID conventions and traceability chain defined in the `sdlc-traceability` skill — stable `FR-###`/`NFR-###`/`TASK-###`/`DR-###` IDs, never renumbered, with every downstream artifact citing upstream IDs instead of restating their content. Do not duplicate that chain here.

## Writing quality
- Use precise, testable, unambiguous language. Prefer measurable targets (thresholds, timeouts, availability) for non-functional requirements.
- Separate **confirmed decisions** from **assumptions** and **open questions** with clear headings.
- Write requirements as outcomes, not implementation guesses, unless the user mandates a technical constraint.

## Formatting
- Use `##`/`###` headings, tables for structured data, and Mermaid fenced blocks for diagrams and data flows.
- Keep documents self-contained and readable by the next agent without reinterpretation.
- Mark anything that could not be found or confirmed as `Not Found` rather than inventing content.