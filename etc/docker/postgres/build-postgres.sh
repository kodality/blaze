#!/usr/bin/env bash

TAG=$1

if [ -z "$TAG" ]; then
   TAG="latest"
fi

docker build -t postgres/blaze:$TAG .

IMAGEID=`docker images -q postgres/blaze:$TAG`
echo "ID of created image: $IMAGEID"

docker tag $IMAGEID postgres/blaze:latest
