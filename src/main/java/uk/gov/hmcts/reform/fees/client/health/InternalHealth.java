package uk.gov.hmcts.reform.fees.client.health;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.health.contributor.Status;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalHealth {
    private final Status status;
    private final Map<String, Object> components;

    @JsonCreator
    public InternalHealth(
        @JsonProperty("status") Status status,
        @JsonProperty("components") Map<String, Object> components
    ) {
        this.status = status;
        this.components = components;
    }

    public Status getStatus() {
        return status;
    }

    public Map<String, Object> getComponents() {
        return components;
    }
}
