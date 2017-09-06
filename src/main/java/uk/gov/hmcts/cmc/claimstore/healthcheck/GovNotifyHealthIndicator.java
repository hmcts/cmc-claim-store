package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

@Component
public class GovNotifyHealthIndicator implements HealthIndicator {

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
        } catch (NotificationClientException exc) {
            return Health.down(exc).build();
        }
    }
}
