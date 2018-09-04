FROM hmcts/cnp-java-base:openjdk-jre-8-alpine-1.1

# Mandatory!
ENV APP claim-store.jar
ENV APPLICATION_TOTAL_MEMORY 512M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 66

COPY build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q --spider http://localhost:4400/health || exit 1

EXPOSE 4400
