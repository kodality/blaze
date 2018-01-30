#!/bin/bash
client=./etc/docker/karaf/console

./etc/docker/karaf/run-karaf.sh
./deploy-docker.sh -b

until $client version | grep 4.1; do sleep 5s; done;
until $client whiplash:list | grep -v "Command not found"; do sleep 2s; done;
$client whiplash:run init-db;
$client whiplash:run pg-store;
$client whiplash:run pg-search;
sleep 3s
until $client blindex:init | grep -v "Command not found\|not yet loaded"; do sleep 2s; done;
$client rest:list
echo OK

