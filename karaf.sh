#!/bin/bash
client=./etc/docker/karaf/console

./etc/docker/karaf/run-karaf.sh
./deploy-docker.sh #-b

until $client version; do sleep 5s; done;
until $client whiplash:list; do sleep 2s; done;
$client whiplash:run init-db;
$client whiplash:run pg-store;
$client whiplash:run pg-search;
sleep 3s
until $client blindex:init; do sleep 2s; done;
$client rest:list
echo OK

