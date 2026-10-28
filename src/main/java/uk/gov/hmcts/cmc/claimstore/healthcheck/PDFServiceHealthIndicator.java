package uk.gov.hmcts.cmc.claimstore.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.PDFServiceProperties;

import java.net.URI;
import java.util.List;

/**
 * Calls pdf-service's /health directly, as PDFServiceClient.serviceHealthy() returns a Spring Boot 3 type.
 */
@Component
public class PDFServiceHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDFServiceHealthIndicator.class);

    private final RestTemplate restTemplate;
    private final URI healthEndpoint;

    public PDFServiceHealthIndicator(RestTemplate restTemplate, PDFServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.healthEndpoint = properties.getUrl().resolve("/health");
    }

    @Override
    public Health health() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            DownstreamHealth health = restTemplate
                .exchange(healthEndpoint, HttpMethod.GET, new HttpEntity<>("", headers), DownstreamHealth.class)
                .getBody();
            return health.toHealth();
        } catch (Exception ex) {
            LOGGER.error("Error on pdf service healthcheck", ex);
            return Health.down(ex).build();
        }
    }
}
