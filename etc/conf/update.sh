#!/bin/bash
cd `dirname $0`

mkdir downloads && cd downloads
wget http://www.hl7.org/fhir/definitions.json.zip &&\
  unzip definitions.json.zip
cd ..


#DEFINITIONS
mkdir definitions
rm -rf definitions/* || true
cp downloads/profiles-resources.json definitions/
cp downloads/profiles-types.json definitions/

#CAPABILITY
mkdir capability
rm -rf capability/* || true
cp -r downloads/profiles-resources.json capability

#SEARCHPARAM
mkdir searchparameter
rm searchparameter/resources.json
cp -r downloads/search-parameters.json searchparameter/resources.json

rm -rf downloads
