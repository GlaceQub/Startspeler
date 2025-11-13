#!/bin/bash
# Start backend and expose with ngrok for remote demo

# Run backend server
(cd ../server && ../gradlew run &)

# Start ngrok tunnel
ngrok http 8080

