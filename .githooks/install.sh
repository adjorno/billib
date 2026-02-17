#!/bin/bash
# Install git hooks for the billib project

HOOKS_DIR=".githooks"
GIT_HOOKS_DIR=".git/hooks"

echo "📦 Installing git hooks..."
echo ""

# Check if .git directory exists
if [ ! -d ".git" ]; then
    echo "❌ Error: .git directory not found. Are you in the project root?"
    exit 1
fi

# Create .git/hooks directory if it doesn't exist
mkdir -p "$GIT_HOOKS_DIR"

# Install pre-commit hook
if [ -f "$HOOKS_DIR/pre-commit" ]; then
    cp "$HOOKS_DIR/pre-commit" "$GIT_HOOKS_DIR/pre-commit"
    chmod +x "$GIT_HOOKS_DIR/pre-commit"
    echo "✅ Installed pre-commit hook"
else
    echo "⚠️  Warning: $HOOKS_DIR/pre-commit not found"
fi

echo ""
echo "✨ Git hooks installation complete!"
echo ""
echo "The pre-commit hook will now run ktlint checks before each commit."
echo "To bypass the hook (not recommended), use: git commit --no-verify"
