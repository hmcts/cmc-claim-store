package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneratePinResponse {

    private final String pin;
    private final Long userId;

    public GeneratePinResponse(@JsonProperty("pin") final String pin, @JsonProperty("userId") final Long userId) {
        this.pin = pin;
        this.userId = userId;
    }

    public String getPin() {
        return pin;
    }

    public Long getUserId() {
        return userId;
    }
}
