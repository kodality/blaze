#!/bin/bash
cd `dirname $0`

dock="blaze-karaf"
KARAF_HOME="/opt/karaf"

deploy_path="$KARAF_HOME/deploy/"

for module in $@; do
  module=`ls | grep $module -m1`
  cd $module
  mvn install -DskipTests && docker cp target/$module-*SNAPSHOT.jar $dock:$deploy_path
  cd -
done


