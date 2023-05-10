 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.12

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

USER hmcts
LABEL maintainer="https://github.com/hmcts/cmc-claim-store"

COPY lib/AI-Agent.xml /opt/app/
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/claim-store.jar /opt/app/

EXPOSE 4400
CMD [ "claim-store.jar" ]
