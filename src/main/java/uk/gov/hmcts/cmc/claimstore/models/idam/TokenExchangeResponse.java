package uk.gov.hmcts.cmc.claimstore.models.idam;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenExchangeResponse {

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonCreator
    public TokenExchangeResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
