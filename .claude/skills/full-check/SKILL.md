---
name: full-check
description: Run comprehensive checks (ktlint, detekt, tests, build) on the entire project and cleanup Gradle daemons. Use before committing, creating PRs, or validating changes.
allowed-tools: Bash
---

# full-check: Comprehensive Project Validation

This skill runs all quality checks and builds to ensure the project is in a healthy state.

## Checks Performed

1. **ktlintCheck** - Verify Kotlin code style compliance
2. **detekt** - Static code analysis (frontend modules only)
3. **test** - Run all unit and integration tests
4. **build** - Compile and build all modules
5. **Gradle cleanup** - Stop daemons to free resources

## Implementation

Run the following commands sequentially:

```bash
# Step 1: ktlint check
echo "Running ktlint..."
./gradlew ktlintCheck

# Step 2: detekt (frontend only due to Kotlin version compatibility)
echo "Running detekt..."
./gradlew detektMetadataCommonMain

# Step 3: Run tests
echo "Running tests..."
./gradlew test

# Step 4: Build project
echo "Building project..."
./gradlew build

# Step 5: Cleanup
echo "Cleaning up Gradle daemons..."
./gradlew --stop
```

## Error Handling

- Continue through all checks even if one fails
- Collect all failures and report at the end
- Always run `./gradlew --stop` cleanup

## Output Format

Provide a summary table:

```
✅ ktlintCheck    - PASSED
✅ detekt         - PASSED
❌ test           - FAILED (3 tests failed)
✅ build          - PASSED

Overall: FAILED (1/4 checks failed)
```

If any check fails, provide details:
- Which module/file failed
- Error message
- Suggested fix (if applicable)
