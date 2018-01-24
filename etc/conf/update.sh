
resources="
patient
practitioner
encounter
observation
organization
valueset
communication
practitionerrole
medication
medicationadministration
medicationdispense
medicationrequest
medicationstatement
immunization
immunizationrecommendation
condition
detectedissue
composition
"


mkdir downloads && cd downloads
wget http://www.hl7.org/fhir/definitions.json.zip &&\
  unzip definitions.json.zip
cd ..


#DEFINITIONS
rm definitions/* || true
for res in $resources; do
  [[ -z "$res" ]] || wget "http://hl7.org/fhir/$res.profile.xml" -O definitions/$res.profile.xml -q  &
done
for job in `jobs -p`; do
  wait $job || echo "some failed"
done

cp downloads/profiles-types.json definitions/

#CAPABILITY
rm capability/*
cp -r downloads/profiles-resources.json capability

#SEARCHPARAM
rm searchparameter/resources.json
cp -r downloads/search-parameters.json searchparameter/resources.json

rm -rf downloads
