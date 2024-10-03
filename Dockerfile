FROM eclipse-temurin:21
# Dependencies
COPY local-repo /root/.m2/repository
COPY etc/karaf/docker/dependencies /root/.m2/repository

# Karaf environment variables
ARG KARAF_VERSION=4.4.6
ARG KARAF_NAME=apache-karaf-${KARAF_VERSION}
ENV KARAF_INSTALL_PATH=/opt
ENV KARAF_HOME=$KARAF_INSTALL_PATH/apache-karaf
ENV PATH=$PATH:$KARAF_HOME/bin

ADD https://dlcdn.apache.org/karaf/${KARAF_VERSION}/${KARAF_NAME}.tar.gz /

RUN mkdir ${KARAF_INSTALL_PATH}/${KARAF_NAME} && \
    tar --strip-components=1 -C ${KARAF_INSTALL_PATH}/${KARAF_NAME} -xzf ${KARAF_NAME}.tar.gz && \
    rm ${KARAF_NAME}.tar.gz && \
    set -x && \
    ln -s "$KARAF_INSTALL_PATH"/apache-karaf* "$KARAF_HOME"

# Add configurations and setup correct versions
COPY etc/conf /opt/apache-karaf/etc
ARG feature_version=2.0.0
RUN sed -i "s/\${feature.version}/$feature_version/" /opt/apache-karaf/etc/org.apache.karaf.features.cfg

CMD ["karaf", "run"]
