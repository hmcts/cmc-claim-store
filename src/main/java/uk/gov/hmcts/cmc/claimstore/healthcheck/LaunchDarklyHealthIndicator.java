package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

public class LaunchDarklyHealthIndicator implements HealthIndicator {
    private static final String TOGGLE = "";
    private final LaunchDarklyClient launchDarklyClient;

    @Autowired
    public LaunchDarklyHealthIndicator(LaunchDarklyClient launchDarklyClient) {
        this.launchDarklyClient = launchDarklyClient;
    }

    @Override
    public Health health() {
        return Health.up().withDetail(TOGGLE, launchDarklyClient.isFeatureEnabled(TOGGLE)).build();
    }
}
