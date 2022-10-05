FROM openjdk:11-slim
RUN apt update && apt install -y wget

ENV KARAF_VERSION=4.3.7

RUN wget https://dlcdn.apache.org/karaf/${KARAF_VERSION}/apache-karaf-${KARAF_VERSION}.tar.gz && \
    mkdir /opt/karaf && \
    tar --strip-components=1 -C /opt/karaf -xzf apache-karaf-${KARAF_VERSION}.tar.gz && \
    rm apache-karaf-${KARAF_VERSION}.tar.gz

EXPOSE 8101
ENTRYPOINT ["/opt/karaf/bin/karaf"]
