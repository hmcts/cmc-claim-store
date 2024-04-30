 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.5.2

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

USER hmcts
LABEL maintainer="https://github.com/hmcts/cmc-claim-store"

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/claim-store.jar /opt/app/

EXPOSE 4400
CMD [ \
    "--add-opens", "java.base/java.lang=ALL-UNNAMED", \
    "--add-opens", "java.base/java.nio=ALL-UNNAMED", \
    "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", \
    "--add-opens", "java.management/sun.management=ALL-UNNAMED", \
    "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED", \
    "claim-store.jar" \
    ]
