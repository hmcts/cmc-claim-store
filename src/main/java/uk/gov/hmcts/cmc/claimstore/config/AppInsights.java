package uk.gov.hmcts.cmc.claimstore.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.logging.appinsights.AbstractAppInsights;

@Component
public class AppInsights extends AbstractAppInsights {

    public AppInsights(TelemetryClient client) {
        super(client);
    }
}
