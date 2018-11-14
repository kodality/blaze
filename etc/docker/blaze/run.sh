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
  --restart unless-stopped \
  blaze/blaze



