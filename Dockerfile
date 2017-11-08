FROM openjdk:8-jre-alpine

COPY build/install/claim-store /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:4400/health

EXPOSE 4400

ENTRYPOINT ["/opt/app/bin/claim-store"]

