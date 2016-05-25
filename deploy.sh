#!/bin/bash

if [ -z "$KARAF_HOME" ]; then
  KARAF_HOME="/opt/karaf"
fi

deploy_path="$KARAF_HOME/deploy/"
p="`dirname \"$0\"`"
v=$(head pom.xml | sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p')

if [[ -z "$1" ]]; then
  mvn install -DskipTests
  cp -v `find $p -name "*$v.jar" | grep "/target/"` $deploy_path
  exit 0
fi

for module in $@; do
  module=`ls $p | grep $module -m1`
  cd $p/$module
  mvn install -DskipTests && cp -v target/$module-$v.jar $deploy_path
  cd -
done


