---
name: diagnosing-bugs
description: >
  Diagnosis loop for hard bugs and performance regressions. Trigger when: the user asks to "diagnose", "debug", "troubleshoot", or reports something broken, crashing, throwing exceptions, or failing.
---

# Diagnosing Bugs

A disciplined process for diagnosing and fixing bugs in Kotlin Multiplatform.

## Phase 1 — Build a feedback loop
Everything else is mechanical. If you have a **tight** pass/fail signal for the bug — one that goes red on *this* bug — you will find the cause. If you don't have one, no amount of staring at code will save you.
1. **Failing test**: Write a failing unit test with Turbine / `kotlin.test` or Maestro UI flow that reaches the bug.
2. **CLI / Script run**: Run `./gradlew desktopTest` or `./scripts/run_maestro_tests.sh` that reproduces the issue.
3. **Replay trace / logs**: Extract a real log output, crash trace, or payload that triggers the failure.

*Phase 1 is complete only when you can run a single fast, deterministic command that fails.*

## Phase 2 — Reproduce + Minimise
1. Verify the loop produces the **exact failure mode** the user described.
2. Shrink the input, data, or config to the **smallest setup** that still triggers the failure.

## Phase 3 — Hypothesise
1. Generate **3–5 ranked hypotheses** before testing any of them.
2. Ensure each hypothesis is falsifiable: *"If X is the cause, then doing Y will make the bug disappear."*
3. Share the hypotheses with the user before testing them to save time.

## Phase 4 — Instrument & Fix
1. Test hypotheses by changing **one variable at a time**.
2. Add debug logging (via Kermit) or assertions to narrow down the state transitions.
3. Implement the fix.

## Phase 5 — Regression Test
1. Run the feedback loop from Phase 1. It must now go **green**.
2. Run `./gradlew desktopTest` to verify no regressions were introduced.
