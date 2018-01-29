#!/bin/bash
dock=blaze-karaf
karaf=/opt/karaf
client=$karaf/bin/client

cp -r /conf/* $karaf/etc
cp -r /run/etc/* $karaf/etc

until $client version; do sleep 5s; done
$client feature:repo-add file://$karaf/etc/features.xml
$client feature:install blaze-deps blaze-pg blaze-liquibase

echo OK


