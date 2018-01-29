#!/bin/bash
name=blaze-karaf

docker rm -vf $name
docker run -d -t \
  --name $name \
  --link blaze-postgres \
  -p 8181:8181 \
  -e JAVA_MIN_MEM="256M" \
  -e JAVA_MAX_MEM="1024M" \
  -e JAVA_PERM_MEM="128M" \
  -e JAVA_MAX_PERM_MEM="256M" \
  mkroli/karaf

docker cp `dirname $0`/run $name:/
docker cp `dirname $0`/../../conf $name:/

docker exec -t $name /bin/sh /run/prepare.sh


