FROM quay.io/debezium/connect:nightly

USER kafka

ENV MAVEN_DEP_DESTINATION="/tmp/jolokia"

RUN mkdir -p "/tmp/jolokia" && \
    docker-maven-download central "org/jolokia" "jolokia-jvm" "1.7.2" "d489d62d1143e6a2e85a869a4b824a67" && \
    ls -la "/tmp/jolokia"

EXPOSE 8778