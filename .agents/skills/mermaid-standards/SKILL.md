---
name: mermaid-standards
description: >
  Guidelines and specifications for creating semantically correct Mermaid flowcharts and class diagrams in technical documentation. Trigger this skill whenever you need to create, refactor, or edit Mermaid flowcharts or class diagrams.
---

# Mermaid Diagram & Flowchart Standardization

This skill defines the strict syntax and semantic rules for creating flowcharts and class diagrams using Mermaid in project documentation.

---

## 1. Flowchart Standards (Semantics & Shapes)

All flowcharts must strictly adhere to the following ISO-standard flowchart shapes and Mermaid syntax rules:

### A. Shape Definitions & Syntax
| Element Type | Visual Representation | Mermaid Syntax | Best Practice |
|---|---|---|---|
| **Terminator** | Capsule / Oval | `([Start])`, `([End])` | Used **only** at the beginning and the end of the flowchart. Never use regular square brackets `[ ]`. |
| **Process** | Rectangle | `[Aksi / Proses]` | Represents internal calculations, state mutations, or actions. Must have **exactly one exit path**. |
| **Input / Output** | Parallelogram | `[/User inputs credentials/]` | Represents reading input from users or external APIs. |
| **Decision** | Diamond / Belah Ketupat | `{"Condition?"}` | Represents conditional branching. Must contain a question and branch into two or more labeled paths (e.g. `-->|Yes|`, `-->|No|`). |
| **Subroutine** | Double-Walled Box | `[[Sub-Process / UseCase]]` | Represents a predefined process defined elsewhere (e.g., calling a UseCase or API sync call). |

### B. Crucial Semantic Rules
1. **No Branching from Processes**: A process block (rectangle `[ ]`) must **never** branch into multiple paths. If a branch is needed, it must go through a decision block (diamond `{" "}`).
2. **Explicit Branch Labels**: Every connection originating from a decision diamond must be clearly labeled using the `-->|Label|` format.
3. **Capsule Terminators**: Ensure all terminators use `([Start])` and `([End])` syntax.

---

## 2. Class Diagram Standards (UML Compliance)

### A. Member Syntax
1. **Fields**: `visibility name: type` (e.g. `- productRepository: ProductRepository`)
2. **Methods**: `visibility name(param: Type) ReturnType`
3. **Visibility Indicators**:
   - `-` for `private`
   - `+` for `public`
   - `#` for `protected`
   - `~` for `internal`

### B. Relationships
- **Implementation**: `Class ..|> Interface`
- **Association**: `ClassA --> ClassB`
- **Dependency**: `ClassA ..> ClassB`
