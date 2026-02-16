# Prompt Implementation Rules

## 1. No Assumptions on Unclear Requirements

### Rule
**When requirements are unclear or information is ambiguous - STOP and ASK. Do NOT assume.**

### Guidelines
- If a requirement can be interpreted in multiple ways, ask for clarification
- If implementation details are missing, request them explicitly
- If user intent is ambiguous, present options and ask which to pursue
- If edge cases are undefined, ask how to handle them

### What Qualifies as Unclear/Ambiguous
- Multiple valid interpretations of a requirement
- Missing technical specifications (API endpoints, data formats, etc.)
- Undefined behavior for edge cases or error states
- Unclear acceptance criteria or success metrics
- Ambiguous scope ("improve performance", "make it better")
- Conflicting requirements or constraints

### Response Pattern
When you encounter unclear requirements:

```
I need clarification before proceeding:

**Question:** [Specific question about the ambiguity]

**Context:** [Why this matters for the implementation]

**Options:** [If applicable, present 2-3 specific approaches]
1. Option A: [Description, pros/cons]
2. Option B: [Description, pros/cons]
3. Option C: [Description, pros/cons]

Which approach would you prefer?
```

### Examples

**❌ BAD - Assuming:**
```
User: "Add caching to the API"
Assistant: *Implements Redis caching with 1-hour TTL*
```

**✅ GOOD - Asking:**
```
User: "Add caching to the API"
Assistant: I need clarification before implementing caching:

**Questions:**
1. Which endpoints should be cached? (all, or specific ones?)
2. What caching strategy? (in-memory, Redis, database, file-based?)
3. What TTL (time-to-live)? (minutes, hours, days?)
4. Cache invalidation strategy? (time-based, manual, event-driven?)
5. Cache for all users or per-user?

Please provide these details so I can implement the right solution.
```

---

## 2. Iterative Development with Manual Review

### Rule
**Plan feature development in iterations where each iteration:**
- Contains **maximum 200 changed/added lines of code**
- Keeps the project in a **working, non-broken state**
- Is **manually reviewed and committed** before proceeding to the next iteration

### Guidelines

#### Iteration Size
- **Maximum 200 lines** changed/added per iteration
- Count only meaningful code (exclude comments, blank lines, imports)
- If a feature requires more, break it into logical iterations
- Each iteration should deliver a complete, testable increment

#### Project State
- After each iteration, the project **MUST compile**
- After each iteration, existing tests **MUST pass**
- After each iteration, the app **MUST be runnable** (if applicable)
- No breaking changes within an iteration
- Use feature flags if needed to hide incomplete features

#### Review Process
After completing each iteration:

1. **STOP and present the changes:**
   - Summary of what was implemented
   - Files changed and line count
   - How to test the changes
   - What's next in the following iteration

2. **WAIT for manual review:**
   - User reviews the code
   - User tests the functionality
   - User approves or requests changes

3. **COMMIT only after approval:**
   - User commits the changes (or asks you to commit)
   - Move to next iteration only after commit

### Iteration Planning Template

When starting a feature, present a plan:

```markdown
## Feature: [Feature Name]

### Iteration Plan

**Iteration 1:** [Description]
- Files: [List of files to change]
- Estimated lines: ~[N] lines
- Deliverable: [What will work after this iteration]
- Testing: [How to verify it works]

**Iteration 2:** [Description]
- Files: [List of files to change]
- Estimated lines: ~[N] lines
- Deliverable: [What will work after this iteration]
- Testing: [How to verify it works]

**Iteration 3:** [Description]
- ...

Shall I proceed with Iteration 1?
```

### After Each Iteration

```markdown
## ✅ Iteration [N] Complete

### Changes Summary
- [File 1]: Added X lines (description)
- [File 2]: Modified Y lines (description)
- **Total: ~[N] lines changed/added**

### What Works Now
- [Feature/functionality that now works]

### Testing Instructions
1. [Step 1 to verify]
2. [Step 2 to verify]
3. Expected result: [What should happen]

### Next Iteration
- [Brief description of what's next]

**Please review, test, and commit these changes before I proceed to Iteration [N+1].**
```

### Examples

**❌ BAD - One big implementation:**
```
User: "Add user authentication with login, signup, password reset, and OAuth"
Assistant: *Implements entire authentication system in one go - 1200 lines*
```

**✅ GOOD - Iterative approach:**
```
User: "Add user authentication with login, signup, password reset, and OAuth"
Assistant: I'll break this into iterations of max 200 lines each:

**Iteration 1:** Basic data models and interfaces (~150 lines)
- Create User model
- Create AuthRepository interface
- Create AuthService interface
- Add to DI configuration

**Iteration 2:** Login UI and ViewModel (~180 lines)
- LoginScreen composable
- LoginViewModel with state management
- Form validation
- Navigation integration

**Iteration 3:** Login API implementation (~160 lines)
- Implement AuthRepository with Ktor
- Handle login API calls
- Token storage
- Error handling

**Iteration 4:** Signup UI and flow (~180 lines)
- SignupScreen composable
- SignupViewModel
- Connect to AuthRepository

**Iteration 5:** Password reset flow (~150 lines)
- Password reset UI
- Email verification
- Reset token handling

**Iteration 6:** OAuth integration (~200 lines)
- Google OAuth setup
- OAuth flow implementation
- Account linking

Shall I start with Iteration 1?
```

---

## Benefits

### No Assumptions Rule
- ✅ Avoids wasted effort on wrong implementations
- ✅ Ensures alignment with user expectations
- ✅ Builds better understanding of requirements
- ✅ Prevents scope creep and feature bloat

### Iterative Development Rule
- ✅ Easier to review small changes
- ✅ Faster feedback loops
- ✅ Easier to spot and fix issues early
- ✅ Always maintain a working codebase
- ✅ Can pivot or adjust based on early iterations
- ✅ Clear progress tracking
- ✅ Reduced risk of merge conflicts

---

## Summary

1. **When in doubt, ASK** - Never assume unclear requirements
2. **Small iterations** - Max 200 lines per iteration
3. **Always working** - Keep project compilable and runnable
4. **Manual review** - Wait for approval before next iteration
5. **Incremental value** - Each iteration delivers something testable
