#!/bin/sh
# Installs the local git pre-commit hook for A+ Study House.
# Copies scripts/pre-commit into .git/hooks/ and makes it executable.
set -e
REPO_ROOT="$(git rev-parse --show-toplevel)"
cp "$REPO_ROOT/scripts/pre-commit" "$REPO_ROOT/.git/hooks/pre-commit"
chmod +x "$REPO_ROOT/.git/hooks/pre-commit"
echo "✅ Pre-commit hook installed at $REPO_ROOT/.git/hooks/pre-commit"
echo "   It runs 'detekt' + 'testDebugUnitTest' before every commit."
echo "   To bypass once: git commit --no-verify"
