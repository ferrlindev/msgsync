#!/bin/sh

echo "Waiting for Pulsar Manager to start..."
sleep 15

echo "Creating superuser account..."
CSRF_TOKEN=$(curl -s http://localhost:7750/pulsar-manager/csrf-token)

curl -s \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -H "Cookie: XSRF-TOKEN=$CSRF_TOKEN;" \
  -H "Content-Type: application/json" \
  -X PUT http://localhost:7750/pulsar-manager/users/superuser \
  -d '{"name": "admin", "password": "apachepulsar", "description": "Admin user", "email": "admin@example.com"}'

echo ""
echo "Superuser created! You can now login at http://localhost:9527"
echo "Username: admin"
echo "Password: apachepulsar"
