package uk.gov.hmcts.cmc.email;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Collections.singletonMap;

@Component
public class AppInsightsService {

    private final TelemetryClient telemetry;

    @Autowired
    public AppInsightsService(TelemetryClient telemetry) {
        this.telemetry = telemetry;
    }

    public void trackEvent(String appInsightsEvent, String referenceType, String value) {
        telemetry.trackEvent(appInsightsEvent, singletonMap(referenceType, value), null);
    }
}
