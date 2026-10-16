package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Replaces the health indicator from payments-java-client, which is built on Spring Boot 3.
 */
@Component("paymentsHealthIndicator")
@ConditionalOnProperty(prefix = "payments", name = "api.url")
public class PaymentsHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsHealthIndicator.class);

    private final PaymentsHealthApi paymentsHealthApi;

    public PaymentsHealthIndicator(PaymentsHealthApi paymentsHealthApi) {
        this.paymentsHealthApi = paymentsHealthApi;
    }

    @Override
    public Health health() {
        try {
            return paymentsHealthApi.health().toHealth();
        } catch (Exception ex) {
            LOGGER.error("Error on payments client healthcheck", ex);
            return Health.down(ex).build();
        }
    }
}
