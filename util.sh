#!/bin/bash

set -e    # Exits immediately if a command exits with a non-zero status.

if [[ "$1" = "local" ]]; then

    mvn clean install -DskipTests
    mvn -f pom.xml docker:build
	docker-compose --project-name data-pipes-setup up --build --force-recreate

elif [ "$1" == "build" ]; then

    mvn clean install -DskipTests

elif [ "$1" == "docker-build" ]; then

    mvn -f pom.xml docker:build

elif [ "$1" == "docker-push" ]; then

    mvn -f pom.xml docker:push

elif [ "$1" == "stage" ]; then

    mvn clean install -DskipTests
    mvn -f pom.xml docker:build -DdockerImageTag=locstaging
    mvn -f pom.xml docker:push -DdockerImageTag=locstaging
fi