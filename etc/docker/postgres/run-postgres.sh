#!/usr/bin/env bash 

CONTAINER_NAME="blaze-postgres"
DB_NAME="blazedb"

docker rm -vf $CONTAINER_NAME
docker run -d \
 -e TZ=Europe/Tallinn \
 -e DB_NAME=$DB_NAME \
 -e POSTGRES_PASSWORD=postgres \
 --restart=unless-stopped \
 --name $CONTAINER_NAME \
 -p 5432:5432 \
 postgres:10.0

docker cp `pwd`/`dirname $0`/docker-entrypoint-initdb.d $CONTAINER_NAME:/
#docker exec -ti $CONTAINER_NAME /init/createdb.sh
#/usr/local/bin/docker-entrypoint.sh: ignoring /docker-entrypoint-initdb.d/* 
