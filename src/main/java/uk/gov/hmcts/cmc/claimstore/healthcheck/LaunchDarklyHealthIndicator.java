package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

@Component
public class LaunchDarklyHealthIndicator implements HealthIndicator {
    private static final String TOGGLE = "launch-darkly-health-roc-8014";
    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    public LaunchDarklyHealthIndicator(LaunchDarklyClient launchDarklyClient) {
        this.launchDarklyClient = launchDarklyClient;
    }

    @Override
    public Health health() {
        return Health.up()
            .withDetail(TOGGLE, launchDarklyClient.isFeatureEnabled(TOGGLE, LaunchDarklyClient.CLAIM_STORE_USER))
            .build();
    }
}
