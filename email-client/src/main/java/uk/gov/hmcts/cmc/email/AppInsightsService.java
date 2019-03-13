package uk.gov.hmcts.cmc.email;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

import static java.util.Collections.singletonMap;

@Component
public class AppInsightsService extends AbstractAppInsights {

    public AppInsightsService(TelemetryClient telemetry) {
        super(telemetry);
    }

    public void trackEvent(String appInsightsEvent, String referenceType, String value) {
        telemetry.trackEvent(appInsightsEvent, singletonMap(referenceType, value), null);
    }
}
