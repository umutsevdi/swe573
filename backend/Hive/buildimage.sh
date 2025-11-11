#/bin/bash
set -a && source test.env && mvnw spring-boot:build-image -Dspring-boot.build-image.imagePlatform=linux/amd64
