#!/bin/bash
#docker build -t andreyznsk/app:llm.latest -f helm/DockerFile .
docker run --name llm -e JVM_OPTIONS="-Xms512m -Xmx2g" -e DB_URL="192.168.0.165:5432" -e AI_URL="http://192.168.0.165:11431" -e PORT=8081 -p 8081:8081 andreyznsk/app:llm.latest
#docker rm llm