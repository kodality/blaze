FROM kodality/karaf:4.3.5

COPY etc /opt/karaf/etc
COPY deploy/com /root/.m2/repository/com/
COPY check_feature.sh /
RUN chmod +x /check_feature.sh

#RUN echo "feature:repo-add file:///opt/karaf/etc/features.xml" | /opt/karaf/bin/karaf
#RUN echo "feature:install blaze-deps blaze-pg blaze-liquibase" | /opt/karaf/bin/karaf && echo 1

RUN echo "./check_feature.sh && feature:repo-add file:///opt/karaf/etc/features.xml\n \
feature:install blaze-deps blaze-pg blaze-liquibase\n \
feature:repo-add mvn:com.kodality.blaze/auth-core/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/auth-rest/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/auth-smart/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/auth-yupi/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/blaze-core/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/blaze-scheduler/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/etc-conformance/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/feature-conditional-reference/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/fhir-rest/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/fhir-structures/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/pg-jdbc/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/pg-search/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/pg-store/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/pg-whiplash/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/resource-binary/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/tx-manager/1.0-SNAPSHOT/xml/features\n \
feature:repo-add mvn:com.kodality.blaze/validation-profile/1.0-SNAPSHOT/xml/features\n \
feature:install auth-core auth-rest auth-smart auth-yupi blaze-core blaze-scheduler etc-conformance feature-conditional-reference fhir-rest fhir-structures pg-jdbc pg-search pg-store pg-whiplash resource-binary tx-manager validation-profile" | /opt/karaf/bin/karaf

#RUN echo "feature:install auth-core auth-rest auth-smart auth-yupi blaze-core blaze-scheduler etc-conformance feature-conditional-reference fhir-rest fhir-structures pg-jdbc pg-search pg-store pg-whiplash resource-binary tx-manager validation-profile" | /opt/karaf/bin/karaf

# /opt/karaf/bin/client stopped working from some version. hacks!
RUN apt-get update --allow-releaseinfo-change && apt -y install openssh-client sshpass \
 && echo 'sshpass -p karaf ssh karaf@localhost -p 8101 -o "StrictHostKeyChecking no" $@' > /client.sh \
 && chmod +x /client.sh

# well, hapi libs need a refresh after startup :/
RUN echo '/client.sh refresh "\"HAPI FHIR - Core Library\"" 2>&1 > /tmp/aaa' > /fix-hapi.sh \
 && chmod +x /fix-hapi.sh
RUN echo '#!/bin/sh' > /startup.sh \
 && echo 'sleep 60 && /fix-hapi.sh &' >> /startup.sh \
 && echo '/opt/karaf/bin/karaf $@' >> /startup.sh \
 && chmod +x /startup.sh

RUN echo "export JAVA_DEBUG_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" >> /opt/karaf/bin/setenv

RUN echo 'alias kc=/opt/karaf/bin/client' >> ~/.bashrc

ENTRYPOINT ["/startup.sh"]

