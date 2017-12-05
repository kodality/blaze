
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

rm definitions/* || true
for res in $resources; do
  [[ -z "$res" ]] || wget "http://hl7.org/fhir/$res.profile.xml" -O definitions/$res.profile.xml -q  &
done
for job in `jobs -p`; do
  wait $job || echo "some failed"
done
