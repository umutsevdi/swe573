#/bin/bash
set -a
source test.env && ./mvnw clean install -DskipTests
source test.env && ./mvnw spring-boot:build-image -DskipTests -Dspring-boot.build-image.imagePlatform=linux/amd64
