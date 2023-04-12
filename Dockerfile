 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

# Application image

FROM hmctspublic.azurecr.io/base/java:11-distroless

USER hmcts
LABEL maintainer="https://github.com/hmcts/cmc-claim-store"

COPY lib/applicationinsights-agent-2.5.1.jar lib/AI-Agent.xml /opt/app/
COPY build/libs/claim-store.jar /opt/app/

EXPOSE 4400
CMD [ "claim-store.jar" ]
