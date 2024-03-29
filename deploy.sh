#!/bin/bash

#KARAF_HOME="/opt/apache-karaf-4.2.5"
KARAF_HOME="/opt/karaf"

deploy_path="$KARAF_HOME/deploy/"
p="`dirname \"$0\"`"
v=$(head pom.xml | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p')

if [[ -z "$1" ]]; then
  mvn clean install -DskipTests && \
  cp -v `find $p/*/target/ -name "*$v.jar"` $deploy_path
  exit 0
fi

for module in $@; do
  module=`ls $p | grep $module -m1`
  cd $p/$module
  mvn clean install -DskipTests && cp -v target/$module-$v.jar $deploy_path
  cd -
done


