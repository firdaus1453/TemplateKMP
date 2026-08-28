---
name: tdd
description: >
  Test-Driven Development (TDD) workflow for Kotlin Multiplatform applications. Trigger when: the user asks for TDD, implementing new business logic, fixing regression bugs, or writing unit tests before code.
---

# Test-Driven Development (TDD) for KMP

Follow the strict **Red-Green-Refactor** cycle for business logic, viewmodels, and data mappers.

---

## 🔁 The TDD Cycle

```
  ┌──────────────┐
  │  1. RED      │  Write a small, failing unit test in commonTest
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │  2. GREEN    │  Write the minimum implementation to pass the test
  └──────┬───────┘
         │
         ▼
  ┌──────────────┐
  │  3. REFACTOR │  Clean up, extract mappers, remove duplication
  └──────┬───────┘
         │
         └────────► Repeat for next requirement
```

---

## 🎯 Guidelines for KMP

1. **Test pure functions & ViewModels first**: Write Turbine tests for state flow emissions and one-off events.
2. **Use in-memory fakes**: Implement fake repositories in `commonTest` without relying on external network or storage.
3. **Verify with fast CLI tasks**: Keep tests running rapidly via `./gradlew desktopTest`.
