#!/bin/bash
dock=blaze-karaf
p="`dirname \"$0\"`"
v=$(head pom.xml | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p')


[[ "$1" == "-b" ]] && mvn clean install -DskipTests
docker cp fhir-stu3/fhir-stu3.jar $dock:/deploy
find $p/*/target/ -name "*$v.jar" -exec docker cp {} $dock:/deploy \;


