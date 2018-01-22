#!/bin/bash
this=`dirname $0`
repo="https://github.com/hl7-fhir/fhir-svn"
fhir="$this/fhir.src"
build="$this/fhir.build"

git clone $repo $fhir || (pushd $fhir && git pull && popd)
ant -f $fhir/build.xml fetch-imports || exit 1

ant -f $fhir/tools/java/org.hl7.fhir.tools.core/build.xml  build
mkdir -p $build
for module in dstu3 utilities validation; do
  cp -r $fhir/implementations/java/org.hl7.fhir.$module/bin/* $build
done;

jar="fhir-stu3.jar"
rm -rf $build/org/hl7/fhir/dstu2 $build/org/hl7/fhir/validation $build/org/hl7/fhir/conversion
pushd $build && jar cfe $jar -C . && popd
mv $build/$jar $this
rm -r $build

