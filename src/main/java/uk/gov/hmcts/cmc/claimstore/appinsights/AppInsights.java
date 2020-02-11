package uk.gov.hmcts.cmc.claimstore.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Collections.singletonMap;

@Component
public class AppInsights {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppInsights.class);

    public static final String REFERENCE_NUMBER = "referenceNumber";
    public static final String EMAIL_REFERENCE = "emailReference";
    public static final String CCD_LINK_DEFENDANT_ID = "ccdLink.defendantId";
    public static final String CLAIM_EXTERNAL_ID = "claim.externalId";
    public static final String DOCUMENT_NAME = "document.name";

    private final TelemetryClient telemetry;
    private final boolean stdout;

    public AppInsights(
        TelemetryClient telemetry,
        @Value("${APPINSIGHTS_INSTRUMENTATIONKEY:}") String instrumentationKey
    ) {
        this.telemetry = telemetry;
        stdout = "STDOUT".equals(instrumentationKey);
    }

    public void trackEvent(AppInsightsEvent appInsightsEvent, String referenceType, String value) {
        trackEvent(appInsightsEvent, singletonMap(referenceType, value));
    }

    public void trackEvent(AppInsightsEvent appInsightsEvent, Map<String, String> properties) {
        telemetry.trackEvent(appInsightsEvent.toString(), properties, null);
        if (stdout) {
            LOGGER.info("AppInsights event: {}: {}", appInsightsEvent, properties);
        }
    }

    public void trackException(Exception exception) {
        telemetry.trackException(exception);
        if (stdout) {
            LOGGER.warn("AppInsights exception: {}", exception.getClass().getSimpleName(), exception);
        }
    }

}
