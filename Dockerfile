FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-2.0

LABEL maintainer="https://github.com/hmcts/cmc-claim-store"

COPY build/libs/claim-store.jar /opt/app
HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q --spider http://localhost:4400/health || exit 1
EXPOSE 4400
CMD [ "claim-store.jar" ]
