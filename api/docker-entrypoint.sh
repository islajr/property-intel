#!/bin/sh

mkdir -p /app/secrets

echo "$JWT_PRIVATE_KEY_BASE64" \
  | base64 -d \
  > /app/secrets/private.pem

echo "$JWT_PUBLIC_KEY_BASE64" \
  | base64 -d \
  > /app/secrets/public.pem

exec java -jar app.jar