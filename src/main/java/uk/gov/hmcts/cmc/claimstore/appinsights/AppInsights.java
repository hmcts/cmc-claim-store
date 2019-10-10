package uk.gov.hmcts.cmc.claimstore.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;

@Component
public class AppInsights {

    public static final String REFERENCE_NUMBER = "referenceNumber";
    public static final String CCD_LINK_DEFENDANT_ID = "ccdLink.defendantId";
    public static final String CLAIM_EXTERNAL_ID = "claim.externalId";
    public static final String DOCUMENT_NAME = "document.name";

    private final TelemetryClient telemetry;

    public AppInsights(TelemetryClient telemetry) {
        this.telemetry = telemetry;
    }

    public void trackEvent(AppInsightsEvent appInsightsEvent, String referenceType, String value) {
        telemetry.trackEvent(appInsightsEvent.toString(), singletonMap(referenceType, value), null);
    }

    public void trackException(Exception exception) {
        telemetry.trackException(exception);
    }

    public void trackEvent(AppInsightsEvent appInsightsEvent, Map<String, String> properties) {
        telemetry.trackEvent(appInsightsEvent.toString(), properties, Collections.emptyMap());
    }

}
