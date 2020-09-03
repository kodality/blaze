FROM kodality/karaf:4.2.9

COPY etc /opt/karaf/etc

RUN echo "feature:repo-add file:///opt/karaf/etc/features.xml" | /opt/karaf/bin/karaf
RUN echo "feature:install blaze-deps blaze-pg blaze-liquibase" | /opt/karaf/bin/karaf && echo 1

COPY deploy /opt/karaf/deploy

# /opt/karaf/bin/client stopped working from some version. hacks!
RUN apt -y install openssh-client sshpass \
 && echo 'sshpass -p karaf ssh karaf@localhost -p 8101 -o "StrictHostKeyChecking no" $@' > /client.sh \
 && chmod +x /client.sh

# well, hapi libs need a refresh after startup :/
RUN echo '/client.sh refresh "\"HAPI FHIR - Core Library\"" 2>&1 > /tmp/aaa' > /fix-hapi.sh \
 && chmod +x /fix-hapi.sh
RUN echo 'sleep 10 && /fix-hapi.sh &' > /startup.sh \
 && echo '/opt/karaf/bin/karaf' >> /startup.sh \
 && chmod +x /startup.sh

ENTRYPOINT ["sh", "-c", "/startup.sh"]

