#!/bin/bash
set -euo pipefail

export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

QUEUE_URL=http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/default-queue

echo "Starting to generate messages..."
COUNTER=0
while true; do
  echo "Sending message ${COUNTER} to SQS"
  aws --region us-east-1 --endpoint-url=http://localhost:4566 sqs send-message \
      --queue-url "${QUEUE_URL}" \
      --message-body "Message $COUNTER"
  echo "Message ${COUNTER} sent"
  ((COUNTER++))
done