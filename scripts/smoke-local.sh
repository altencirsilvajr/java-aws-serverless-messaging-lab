#!/usr/bin/env bash
set -euo pipefail

api_url="${API_URL:-http://localhost:8080}"
message_id="smoke-$(date +%s)"
failed_id="$message_id-fail"

wait_for() {
  local description="$1"
  local command="$2"
  for _ in {1..40}; do
    if eval "$command" >/dev/null 2>&1; then
      echo "smoke: $description"
      return 0
    fi
    sleep 1
  done
  echo "smoke: timeout waiting for $description" >&2
  return 1
}

wait_for "API ready" "curl -fsS '$api_url/q/health/ready'"

curl -fsS -X POST "$api_url/api/commands" -H 'Content-Type: application/json' \
  -d "{\"messageId\":\"$message_id\",\"payload\":\"rebuild-index\",\"simulateFailure\":false}" >/dev/null
wait_for "successful command completed" "curl -fsS '$api_url/api/commands/$message_id' | grep -q '\"status\":\"COMPLETED\"'"

curl -fsS -X POST "$api_url/api/commands/$message_id/duplicate" >/dev/null
wait_for "transport duplicate recorded" "curl -fsS '$api_url/api/commands/$message_id' | grep -Eq '\"duplicateCount\":[1-9]'"

curl -fsS -X POST "$api_url/api/commands" -H 'Content-Type: application/json' \
  -d "{\"messageId\":\"$failed_id\",\"payload\":\"force-dlq\",\"simulateFailure\":true}" >/dev/null
wait_for "failed command retried three times" "curl -fsS '$api_url/api/commands/$failed_id' | grep -Eq '\"attempts\":[3-9]'"
wait_for "failed command moved to DLQ" "curl -fsS '$api_url/api/commands/operations/queues' | grep -Eq '\"deadLetter\":[1-9]'"

echo "smoke: completed, duplicate and DLQ flows passed"
