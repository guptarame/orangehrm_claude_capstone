---
description: "Conventions for automated tests generated during implementation and verification."
paths:
  - "src/test/java/**/*.java"
  - "src/test/resources/features/**/*.feature"
---

# Test Standards
Applies to every automated test in the repository.

## Coverage
- Derive test cases directly from acceptance criteria and requirement IDs. Reference the ID in the test name or a one-line comment (`// FR-002` in Java, `# FR-002` in `.feature` files).
- Cover the **happy path AND** the edge cases: `Not Found`, missing fields, empty inputs, invalid states, timeouts, and error branches.
- Every functional and non-functional requirement must map to at least one passing test.

## Structure
- Provide **unit tests** (Surefire `**/*Test.java` pattern, e.g. `ConfigReaderTest.java`) for pure logic with no browser or network
  involved.
- Cucumber scenarios in `features/*.feature`, bound via step definitions, are this project's end-to-end tests. They intentionally drive the real public demo host (`base.url`) over HTTPS — there is no fixture or mock layer substituting for it; that live demo instance **is** the target under test.
- Follow the project's existing framework, runner, naming, and directory layout. Do not introduce a new framework without cause.

## Quality
- Keep tests independent, deterministic, and clearly named. No hidden ordering or timing dependencies.
- Never make a test pass by weakening assertions, deleting coverage, or masking a real defect. Fix the root cause.
- Report coverage honestly, including any untested critical paths.