FROM openjdk:8-jre-alpine

COPY build/install/claim-store /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q http://localhost:4400/health || exit 1

EXPOSE 4400

ENTRYPOINT ["/opt/app/bin/claim-store"]

