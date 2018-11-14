FROM kodality/karaf

COPY etc /opt/karaf/etc
RUN /opt/karaf/bin/start && \
    until /opt/karaf/bin/client version; do sleep 1; done && \
    /opt/karaf/bin/client feature:repo-add file:///opt/karaf/etc/features.xml && \
    /opt/karaf/bin/client feature:install blaze-deps blaze-pg blaze-liquibase && \
    /opt/karaf/bin/stop

COPY deploy /opt/karaf/deploy

