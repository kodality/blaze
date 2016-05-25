#!/usr/bin/env bash 

LOCAL_PORT="5432"
CONTAINER_NAME="blaze-postgres"
DB_NAME="blazedb"

docker run -e TZ=Europe/Tallinn -e DB_NAME=$DB_NAME -e POSTGRES_PASSWORD=mysecretpassword -p $LOCAL_PORT:5432 \
 -v `pwd`/`dirname $0`/docker-entrypoint-initdb.d:/docker-entrypoint-initdb.d \
 --restart=unless-stopped \
 --name $CONTAINER_NAME -d postgres:9.5.2
