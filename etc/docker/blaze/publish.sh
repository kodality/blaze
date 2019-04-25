#!/bin/bash
img=kodality/blaze
cd `dirname $0`

#docker login || exit 1
./build.sh && \
docker push $img
