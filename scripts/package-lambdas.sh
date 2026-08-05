#!/usr/bin/env bash
set -euo pipefail

java_home="${JAVA_HOME:-/opt/homebrew/opt/openjdk@25}"
output_dir="target/lambda"
temporary_dir="$(mktemp -d)"
trap 'rm -rf "$temporary_dir"' EXIT

JAVA_HOME="$java_home" ./mvnw -q clean package -DskipTests -Plambda-api
cp target/function.zip "$temporary_dir/api.zip"

JAVA_HOME="$java_home" QUARKUS_LAMBDA_HANDLER=worker ./mvnw -q clean package -DskipTests -Plambda-worker
mkdir -p "$output_dir"
cp "$temporary_dir/api.zip" "$output_dir/api.zip"
cp target/function.zip "$output_dir/worker.zip"

echo "Lambda packages: $output_dir/api.zip and $output_dir/worker.zip"
