package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

@Component
public class PDFServiceHealthIndicator implements HealthIndicator {

    private final PDFServiceClient client;

    @Autowired
    public PDFServiceHealthIndicator(PDFServiceClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        // SPIKE: pdf-service-client 8.0.2 returns Boot 3 actuate.health.Health - needs a Boot 4 release
        return Health.unknown().build();
    }
}
