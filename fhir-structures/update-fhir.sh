#!/bin/bash
this=`dirname $0`
repo="https://github.com/hl7-fhir/fhir-svn"
fhir="$this/fhir.src"
build="$this/lib/build"

git clone $repo $fhir
ant -f $fhir/build.xml fetch-imports || exit 1

mkdir -p $build
for module in dstu3 utilities validation; do
  path=$fhir/implementations/java/org.hl7.fhir.$module
  ant -f $path/build.xml || exit 1
  cp -r $path/bin/* $build
done;

jar="fhir-stu3.jar"
rm -rf $build/dstu2 $build/validation $build/conversion
pushd $build && jar cfe $jar -C . && popd
mv $build/$jar $this/lib/
rm -r $build

