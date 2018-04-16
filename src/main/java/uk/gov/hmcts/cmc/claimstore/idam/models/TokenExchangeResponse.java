package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenExchangeResponse {

    @JsonProperty("access_token")
    private String accessToken;

    public TokenExchangeResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

}
