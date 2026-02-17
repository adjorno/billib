---
name: ktlint-fix
description: Auto-fix ktlint violations across the entire project, verify with ktlintCheck, and cleanup Gradle daemons. Use when formatting code or fixing linting issues.
allowed-tools: Bash
---

# ktlint-fix: Auto-format and Verify Code

This skill automatically formats all Kotlin code in the project using ktlint, verifies the fixes, and cleans up Gradle daemon processes.

## Steps

1. **Run ktlintFormat** - Auto-fix all ktlint violations across all modules
2. **Run ktlintCheck** - Verify that all issues are resolved
3. **Cleanup Gradle** - Stop Gradle daemons to free resources
4. **Report results** - Show success or list any unfixable issues

## Implementation

Run the following commands sequentially:

```bash
# Step 1: Auto-fix ktlint violations
./gradlew ktlintFormat

# Step 2: Verify all issues are fixed
./gradlew ktlintCheck

# Step 3: Cleanup Gradle daemons
./gradlew --stop
```

## Error Handling

- If `ktlintFormat` succeeds but `ktlintCheck` still reports issues, list the unfixable issues
- If `ktlintFormat` fails, report which files/modules failed and why
- Always run `./gradlew --stop` at the end, even if previous commands fail

## Output

Report one of:
- ✅ **Success**: "All ktlint issues fixed and verified"
- ⚠️ **Partial success**: "Fixed X issues, but Y remain unfixable: [list issues]"
- ❌ **Failure**: "ktlintFormat failed: [error message]"
