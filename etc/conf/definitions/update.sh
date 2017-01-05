for res in patient practitioner encounter observation organization valueset communication; do
  wget "http://hl7.org/fhir/2017Jan/$res.profile.xml"
done
