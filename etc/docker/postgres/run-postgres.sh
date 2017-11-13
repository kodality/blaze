#!/usr/bin/env bash 

LOCAL_PORT="5432"
CONTAINER_NAME="blaze-postgres"
DB_NAME="blazedb"

docker run -d \
 -e TZ=Europe/Tallinn \
 -e DB_NAME=$DB_NAME \
 -e POSTGRES_PASSWORD=postgres \
 -v `pwd`/`dirname $0`/docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d \
 --restart=unless-stopped \
 --name $CONTAINER_NAME \
 -p 5432:5432 \
 postgres:10.0
