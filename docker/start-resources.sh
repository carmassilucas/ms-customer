#!/bin/bash

aws --endpoint-url=http://localhost:4566 --region us-east-1 s3 mb s3://customer-profile-picture

aws --endpoint-url=http://localhost:4566 --region us-east-1 dynamodb create-table \
  --table-name customer \
  --attribute-definitions AttributeName=email,AttributeType=S \
  --key-schema AttributeName=email,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

aws --endpoint-url=http://localhost:4566 --region us-east-2 dynamodb create-table \
  --table-name customer \
  --attribute-definitions AttributeName=email,AttributeType=S \
  --key-schema AttributeName=email,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
