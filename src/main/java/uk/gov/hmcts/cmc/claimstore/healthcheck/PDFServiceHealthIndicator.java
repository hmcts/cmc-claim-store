package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
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
        return client.serviceHealthy();
    }
}
