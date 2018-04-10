package uk.gov.hmcts.cmc.claimstore.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

import static java.util.Collections.singletonMap;

@Component
public class AppInsights extends AbstractAppInsights {

    public AppInsights(TelemetryClient telemetryClient) {
        super(telemetryClient);
    }

    public void trackEvent(AppInsightsEvent appInsightsEvent, String claimNumber) {
        telemetry.trackEvent(appInsightsEvent.toString(), singletonMap("referenceNumber", claimNumber), null);
    }
}
