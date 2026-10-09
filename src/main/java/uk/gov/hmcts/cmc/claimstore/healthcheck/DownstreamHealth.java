package uk.gov.hmcts.cmc.claimstore.healthcheck;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.health.contributor.Health;

/**
 * Body of a downstream service's /health response. The pdf-service, payments and send-letter
 * clients only ship Spring Boot 3 health types, so their health checks are done here instead.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DownstreamHealth {

    private final String status;

    @JsonCreator
    public DownstreamHealth(@JsonProperty("status") String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Health toHealth() {
        return Health.status(status).build();
    }
}
