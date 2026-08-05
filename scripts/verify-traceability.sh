#!/usr/bin/env bash
set -euo pipefail

mode="${1:-}"
if [[ "$mode" != "--staged" ]]; then
  echo "usage: $0 --staged" >&2
  exit 2
fi

mapfile_command="mapfile"
if ! command -v "$mapfile_command" >/dev/null 2>&1; then
  changed_files=()
  while IFS= read -r file; do changed_files+=("$file"); done < <(git diff --cached --name-only --diff-filter=ACMR)
else
  mapfile -t changed_files < <(git diff --cached --name-only --diff-filter=ACMR)
fi

if [[ ${#changed_files[@]} -eq 0 ]]; then
  echo "traceability: no staged files" >&2
  exit 1
fi

journal_count=0
for file in "${changed_files[@]}"; do
  [[ "$file" == journal/*.md ]] && journal_count=$((journal_count + 1))
done

if [[ "$journal_count" -ne 1 ]]; then
  echo "traceability: expected exactly one staged journal, found $journal_count" >&2
  exit 1
fi

echo "traceability: exactly one staged journal"
