#!/bin/bash
cd `dirname $0`

name=blaze-karaf
image=kodality/blaze

docker rm -vf $name
#docker pull $image
docker run -d -t \
  --name $name \
  --link blaze-postgres \
  -p 8181:8181 \
  -p 5007:5005 \
  -e JAVA_MIN_MEM="256M" \
  -e JAVA_MAX_MEM="1024M" \
  -e JAVA_PERM_MEM="128M" \
  -e JAVA_MAX_PERM_MEM="256M" \
  --restart unless-stopped \
  $image $@

docker exec -ti $name sh -c 'echo "
db.url=jdbc:postgresql://blaze-postgres:5432/blazedb
db.username=blaze
db.password=blaze
db.maxActive=4
" > /opt/karaf/etc/com.kodality.blaze.pg.cfg'

docker exec -ti $name sh -c 'echo "
oidc.url=http://172.17.0.1:4567
" > /opt/karaf/etc/com.kodality.blaze.auth.openid.cfg'

