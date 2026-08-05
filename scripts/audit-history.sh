#!/usr/bin/env bash
set -euo pipefail

git log --format='%H' --no-merges --reverse | while read -r commit; do
  count="$(git diff-tree --root --no-commit-id --name-only -r "$commit" | awk '/^journal\/.*\.md$/ { count++ } END { print count + 0 }')"
  if [[ "$count" -ne 1 ]]; then
    echo "$commit has $count journal files" >&2
    exit 1
  fi
done

echo "history: every non-merge commit contains exactly one journal"
