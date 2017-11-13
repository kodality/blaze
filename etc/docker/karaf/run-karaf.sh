#!/bin/bash

docker rm -vf blaze-karaf
docker run -d -t \
  --name blaze-karaf \
  --link blaze-postgres \
  -p 8181:8181 \
  -v `pwd`/`dirname $0`/run:/run \
  blaze/karaf

docker exec -t blaze-karaf /bin/sh /run/deploy-blaze.sh
