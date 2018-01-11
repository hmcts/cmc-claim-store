package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;

@Component
public class GovNotifyHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovNotifyHealthIndicator.class);

    private final NotificationClient client;

    @Autowired
    public GovNotifyHealthIndicator(NotificationClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        try {
            client.getNotifications(null, null, "fake_ref", null);
            return Health.up().build();
        } catch (Exception exc) {
            LOGGER.error("Error on GOV Notify service healthcheck", exc);
            return Health.down(exc).build();
        }
    }
}
