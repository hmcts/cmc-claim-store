package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Replaces the health indicator from send-letter-client, which is built on Spring Boot 3.
 */
@Component("sendLetterHealthIndicator")
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class SendLetterHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendLetterHealthIndicator.class);

    private final SendLetterHealthApi sendLetterHealthApi;

    public SendLetterHealthIndicator(SendLetterHealthApi sendLetterHealthApi) {
        this.sendLetterHealthApi = sendLetterHealthApi;
    }

    @Override
    public Health health() {
        try {
            return sendLetterHealthApi.health().toHealth();
        } catch (Exception ex) {
            LOGGER.error("Error on send letter healthcheck", ex);
            return Health.down(ex).build();
        }
    }
}
