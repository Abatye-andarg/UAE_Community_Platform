#!/bin/bash
# Load .env and start the Spring Boot app
# Usage: ./run.sh

set -a              # auto-export all variables
source .env
set +a       #stop auto-exporting variables

mvn spring-boot:run
