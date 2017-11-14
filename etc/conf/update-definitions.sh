rm definitions/*
for res in patient practitioner encounter observation organization valueset communication medication practitionerrole; do
  wget "http://hl7.org/fhir/$res.profile.xml" -O definitions/$res.profile.xml -q  &
done
for job in `jobs -p`; do
  wait $job || echo "some failed"
done
