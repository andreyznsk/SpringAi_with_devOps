#!/bin/bash
docker run \
  --name postgresql-container \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=ragdb \
  -p 5444:5432 \
  -d ankane/pgvector:v0.5.0