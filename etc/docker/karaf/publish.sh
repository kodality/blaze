#!/bin/bash
this=`pwd`/`dirname $0`

repo="kodality"
name="karaf"
tag="4.2.0"

#docker login || exit 1
docker build -t $repo/$name . || exit 1

docker tag $repo/$name $repo/$name:$tag && \
docker push $repo/$name:$tag

docker tag $repo/$name $repo/$name:latest && \
docker push $repo/$name:latest
