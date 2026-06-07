#!/bin/bash
# Start local dev environment: backend, frontend, and MySQL

# Start MySQL (if needed)
docker-compose -f ../docker-compose.mysql.yml up -d

# Run backend server
(cd ../server && ../gradlew run &)

# Run frontend dev server
../gradlew :jsApp:jsBrowserDevelopmentRun

