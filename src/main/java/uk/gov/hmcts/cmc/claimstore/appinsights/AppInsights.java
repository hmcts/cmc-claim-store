package uk.gov.hmcts.cmc.claimstore.appinsights;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

import static java.util.Collections.singletonMap;

@Component
public class AppInsights extends AbstractAppInsights {
    public static final String REFERENCE_NUMBER = "referenceNumber";
    public static final String CCD_LINK_DEFENDANT_ID = "ccdLink.defendantId";
    public static final String CLAIM_EXTERNAL_ID = "claim.externalId";

    public AppInsights(TelemetryClient telemetryClient) {
        super(telemetryClient);
    }

    public void trackEvent(AppInsightsEvent appInsightsEvent, String referenceType, String claimNumber) {
        telemetry.trackEvent(appInsightsEvent.toString(), singletonMap(referenceType, claimNumber), null);
    }
}
