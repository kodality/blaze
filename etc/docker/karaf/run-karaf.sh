#!/bin/bash
name=blaze-karaf

docker rm -vf $name
docker run -d -t \
  --name $name \
  --link blaze-postgres \
  -p 8181:8181 \
  mkroli/karaf

docker cp `dirname $0`/run $name:/
docker cp `dirname $0`/../../conf $name:/

docker exec -t $name /bin/sh /run/prepare.sh


