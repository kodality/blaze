
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
"

#DEFINITIONS
rm definitions/* || true
for res in $resources; do
  [[ -z "$res" ]] || wget "http://hl7.org/fhir/$res.profile.xml" -O definitions/$res.profile.xml -q  &
done
for job in `jobs -p`; do
  wait $job || echo "some failed"
done

mkdir downloads && cd downloads
wget https://www.hl7.org/fhir/definitions.json.zip &&\
  unzip definitions.json.zip
cd ..

#CAPABILITY
rm capability/*
cp -r downloads/profiles-resources.json capability

#SEARCHPARAM
rm searchparam/resources.json
cp -r downlaods/search-parameters.json searchparam/resources.json

rm -rf downloads
