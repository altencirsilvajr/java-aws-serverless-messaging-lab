#!/usr/bin/env bash
set -euo pipefail

region="${AWS_DEFAULT_REGION:-us-east-1}"

awslocal sqs create-queue --queue-name processing-commands-dlq --region "$region" >/dev/null
dlq_url="$(awslocal sqs get-queue-url --queue-name processing-commands-dlq --query QueueUrl --output text --region "$region")"
dlq_arn="$(awslocal sqs get-queue-attributes --queue-url "$dlq_url" --attribute-names QueueArn --query Attributes.QueueArn --output text --region "$region")"

printf '{"QueueName":"processing-commands","Attributes":{"VisibilityTimeout":"2","RedrivePolicy":"{\\"deadLetterTargetArn\\":\\"%s\\",\\"maxReceiveCount\\":\\"3\\"}"}}' \
  "$dlq_arn" >/tmp/processing-queue.json
awslocal sqs create-queue --cli-input-json file:///tmp/processing-queue.json --region "$region" >/dev/null

if ! awslocal dynamodb describe-table --table-name processing-state --region "$region" >/dev/null 2>&1; then
  awslocal dynamodb create-table \
    --table-name processing-state \
    --attribute-definitions AttributeName=messageId,AttributeType=S \
    --key-schema AttributeName=messageId,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region "$region" >/dev/null
fi

echo "Local AWS resources are ready"
