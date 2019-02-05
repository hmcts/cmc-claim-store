package uk.gov.hmcts.cmc.dm.batch.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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
