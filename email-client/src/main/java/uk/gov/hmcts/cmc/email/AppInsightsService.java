package uk.gov.hmcts.cmc.email;

import com.microsoft.applicationinsights.TelemetryClient;

import static java.util.Collections.singletonMap;

public class AppInsightsService {

    private final TelemetryClient telemetry;

    public AppInsightsService(TelemetryClient telemetry) {
        this.telemetry = telemetry;
    }

    public void trackEvent(String appInsightsEvent, String referenceType, String value) {
        telemetry.trackEvent(appInsightsEvent, singletonMap(referenceType, value), null);
    }
}
