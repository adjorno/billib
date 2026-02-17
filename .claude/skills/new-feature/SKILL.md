---
name: new-feature
description: Create a new feature branch and generate an iteration plan following the 200-line rule. Use when starting work on a new feature or significant change.
allowed-tools: Bash, Read, Grep, Glob, AskUserQuestion
argument-hint: <feature-name> <description>
---

# new-feature: Start New Feature with Iteration Plan

This skill helps you start a new feature by creating a feature branch and generating a proper iteration plan that follows project coding standards and the 200-line rule.

## Usage

```bash
/new-feature <feature-name> <description>
```

**Examples:**
- `/new-feature user-authentication Add login and signup functionality`
- `/new-feature chart-caching Implement SQLDelight caching for charts`

## Workflow

### Step 1: Create Feature Branch

1. Check current branch and git status
2. Ensure we're on `main` branch (or switch to it)
3. Pull latest changes from `main`
4. Create feature branch: `feature/<feature-name>`

```bash
git checkout main
git pull origin main
git checkout -b feature/$ARGUMENTS[0]
```

### Step 2: Understand Requirements

If the description is unclear or ambiguous:
- **STOP and ASK** for clarification (see `.claude/rules/Prompt_Implementation.md`)
- Ask about:
  - Unclear requirements or multiple interpretations
  - Missing technical specifications
  - Undefined edge cases
  - Acceptance criteria

### Step 3: Explore Codebase

Read relevant files to understand:
- Existing architecture patterns
- Similar features (to maintain consistency)
- Dependencies and interfaces
- Coding standards (`.claude/rules/Coding.md`)

### Step 4: Generate Iteration Plan

Create a plan following the **200-line rule**:
- Each iteration: **max 200 changed/added lines**
- Each iteration: keep project **compilable and runnable**
- Each iteration: deliver **testable increment**

**Plan Template:**

```markdown
## Feature: <feature-name>

### Requirements
<Summary of what needs to be implemented>

### Iteration Plan

**Iteration 1:** <Description>
- Files: <List of files to change>
- Estimated lines: ~<N> lines
- Deliverable: <What will work after this iteration>
- Testing: <How to verify it works>

**Iteration 2:** <Description>
- Files: <List of files to change>
- Estimated lines: ~<N> lines
- Deliverable: <What will work after this iteration>
- Testing: <How to verify it works>

<...more iterations as needed...>

### Architecture Notes
- Follows dependency inversion: <explain interfaces>
- Uses immutable state: <explain state management>
- Respects class size limit: <explain how classes stay under 100 lines>
- Named arguments: <remind about 3+ parameter rule>

Shall I proceed with Iteration 1?
```

### Step 5: Coding Standards Reminders

Ensure the plan follows:
- ✅ **Dependency Inversion**: Features define their own interfaces
- ✅ **Immutability**: `val` over `var`, sealed classes for state
- ✅ **Interface Naming**: Clean names (no `I-` prefix, no `-Interface` suffix)
- ✅ **Implementation Naming**: Descriptive (e.g., `KtorBillibApi`, not `BillibApiImpl`)
- ✅ **Class Size**: Max 100 meaningful lines per class
- ✅ **Named Arguments**: Required for functions with 3+ parameters
- ✅ **Trailing Commas**: Enforced by ktlint on multi-line calls

## Output

Present the iteration plan and wait for user approval before proceeding with Iteration 1.
