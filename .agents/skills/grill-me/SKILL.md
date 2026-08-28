---
name: grill-me
description: >
  Grill the user relentlessly about a plan, decision, or feature idea to reach a shared understanding. Trigger when: the user asks to "grill me", "interview me", wants to stress-test their plan/design, or before starting a complex task.
---

# Grill Me — Relentless Planning & Requirements Alignment

Interview the user relentlessly about every aspect of their request/plan/design until a shared, crystal-clear understanding is reached. Walk down each branch of the decision tree, resolving dependencies between decisions one-by-one.

For each question, provide your recommended option/answer first so the user can easily review and choose.

## 🚦 Rules of the Grill
1. **One question at a time**: Ask the questions one at a time. Waiting for feedback on each question before continuing. Asking multiple questions at once is bewildering and leads to cognitive fatigue.
2. **Search first**: If a fact can be found by exploring the environment (code base, Gradle files, dependencies, database entities), look it up using tools rather than asking the user.
3. **Decisions are the user's**: Put each design choice, feature decision, and tradeoff to the user and wait for their answer.
4. **No immediate action**: Do not write any feature code or execute changes until the user explicitly confirms that a shared understanding is reached.
