FROM mkroli/karaf

RUN apt-get update && apt-get install -y maven
COPY features.xml /opt/karaf/etc/
RUN /opt/karaf/bin/start; until /opt/karaf/bin/client version; do sleep 5s; done; \
    /opt/karaf/bin/client feature:repo-add file:///opt/karaf/etc/features.xml; \
    /opt/karaf/bin/client feature:install blaze-deps blaze-pg blaze-liquibase;
