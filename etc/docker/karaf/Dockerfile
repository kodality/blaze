FROM java:8-jdk
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV KARAF_VERSION=4.0.5

RUN apt-get update && apt-get install -y maven

RUN wget http://archive.apache.org/dist/karaf/${KARAF_VERSION}/apache-karaf-${KARAF_VERSION}.tar.gz; \
    mkdir /opt/karaf && tar --strip-components=1 -C /opt/karaf -zxvf apache-karaf-${KARAF_VERSION}.tar.gz; \
    rm apache-karaf-${KARAF_VERSION}.tar.gz

RUN git clone https://github.com/nortal/blaze/; \
    cd blaze && git checkout develop && mvn clean install -DskipTests; \
    cp -r etc/conf/* /opt/karaf/etc; \
    cp etc/features.xml /opt/karaf/etc;

RUN /opt/karaf/bin/start; until /opt/karaf/bin/client version; do sleep 5s; done; \
    /opt/karaf/bin/client feature:repo-add file:///opt/karaf/etc/features.xml; \
    /opt/karaf/bin/client feature:install blaze-deps blaze-pg blaze-liquibase;

EXPOSE 8181 
ENTRYPOINT ["/opt/karaf/bin/karaf"]
