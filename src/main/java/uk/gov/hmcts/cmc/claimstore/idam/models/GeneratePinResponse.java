package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class GeneratePinResponse {

    private final String pin;
    private final String userId;

    public GeneratePinResponse(@JsonProperty("pin") String pin, @JsonProperty("userId") String userId) {
        this.pin = pin;
        this.userId = userId;
    }

    public String getPin() {
        return pin;
    }

    public String getUserId() {
        return userId;
    }
}
