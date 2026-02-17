# Git Hooks

This directory contains git hooks to maintain code quality and prevent common issues.

## Available Hooks

### pre-commit

Runs before each commit to check code quality:
- ✅ Runs `ktlintCheck` to verify Kotlin code style
- ❌ Blocks commit if ktlint finds violations
- 💡 Suggests fixes (`./gradlew ktlintFormat` or `/ktlint-fix`)

## Installation

### Quick Install

```bash
.githooks/install.sh
```

### Manual Install

```bash
cp .githooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

## Usage

Once installed, the hooks run automatically:

```bash
# Commit normally - hook runs automatically
git commit -m "Fix bug"

# If ktlint fails, fix automatically
./gradlew ktlintFormat

# Or bypass the hook (not recommended)
git commit --no-verify -m "Fix bug"
```

## Benefits

- ✅ Catches code style issues before CI
- ✅ Faster feedback loop (local vs remote)
- ✅ Prevents failed CI builds
- ✅ Maintains consistent code style across the team

## Troubleshooting

**Hook doesn't run:**
- Check if `.git/hooks/pre-commit` exists and is executable
- Run installation script again

**Hook fails with permission error:**
- Run: `chmod +x .git/hooks/pre-commit`

**Want to disable temporarily:**
- Use `git commit --no-verify` (not recommended)
- Or remove `.git/hooks/pre-commit` temporarily
