package uk.gov.hmcts.cmc.claimstore.appinsights;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Collections.singletonMap;

@Component
public class AppInsights {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AppInsights.class);

    public static final String REFERENCE_NUMBER = "referenceNumber";
    public static final String EMAIL_REFERENCE = "emailReference";
    public static final String CCD_LINK_DEFENDANT_ID = "ccdLink.defendantId";
    public static final String CLAIM_EXTERNAL_ID = "claim.externalId";
    public static final String DOCUMENT_NAME = "document.name";

    private final Logger telemetry;
    private final boolean stdout;

    public AppInsights(
        Logger telemetry,
        @Value("${APPINSIGHTS_INSTRUMENTATIONKEY:}") String instrumentationKey
    ) {
        this.telemetry = telemetry;
        stdout = "STDOUT".equals(instrumentationKey);
    }

    public void trackEvent(AppInsightsEvent appInsightsEvent, String referenceType, String value) {
        trackEvent(appInsightsEvent, singletonMap(referenceType, value));

    }

    public void trackEvent(AppInsightsEvent appInsightsEvent, Map<String, String> properties) {
        AttributesBuilder attributes = Attributes.builder();
        properties.forEach((key, value) -> {
            if (value != null) {
                attributes.put(AttributeKey.stringKey(key), value);
            }
        });
        telemetry.logRecordBuilder()
            .setSeverity(Severity.INFO)
            .setBody(appInsightsEvent.toString())
            .setAllAttributes(attributes.build())
            .emit();
        if (stdout) {
            LOGGER.info("AppInsights event: {}: {}", appInsightsEvent, properties);
        }
    }

    public void trackException(Exception exception) {
        telemetry.logRecordBuilder()
            .setSeverity(Severity.ERROR)
            .setBody(exception.getClass().getSimpleName())
            .setException(exception)
            .emit();
        if (stdout) {
            LOGGER.warn("AppInsights exception: {}", exception.getClass().getSimpleName(), exception);
        }
    }

}
