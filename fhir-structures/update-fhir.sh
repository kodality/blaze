#!/bin/bash

repo="https://github.com/hl7-fhir/fhir-svn"
fhir="fhir.src"

git clone $repo $fhir
ant -f $fhir/build.xml fetch-imports || exit 1

mkdir -p lib/build
for module in dstu3 utilities; do
  path=$fhir/implementations/java/org.hl7.fhir.$module
  ant -f $path/build.xml || exit 1
  cp -r $path/bin/* lib/build/
done;

jar="fhir-stu3.jar"
pushd lib/build && jar cfe $jar -C . && popd
mv lib/build/$jar lib/
rm -r lib/build

