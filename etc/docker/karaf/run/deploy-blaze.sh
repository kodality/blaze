cp `dirname $0`/com.nortal.blaze.pg.cfg /opt/karaf/etc/
cd blaze
./deploy.sh
cp fhir-stu3/fhir-stu3.jar /opt/karaf/deploy
until /opt/karaf/bin/client version; do sleep 5s; done;
until /opt/karaf/bin/client whiplash:list; do sleep 2s; done;
/opt/karaf/bin/client whiplash:run init-db;
/opt/karaf/bin/client whiplash:run pg-store;
/opt/karaf/bin/client whiplash:run pg-search;
/opt/karaf/bin/client restart conformance;
/opt/karaf/bin/client restart pg-search;
until /opt/karaf/bin/client blindex:init; do sleep 2s; done;

