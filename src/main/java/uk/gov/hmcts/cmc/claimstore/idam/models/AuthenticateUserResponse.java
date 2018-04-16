package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateUserResponse {

    private String code;
    @JsonProperty("access_token")
    private String accessToken;

    public AuthenticateUserResponse(String code, String accessToken) {
        this.code = code;
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getCode() {
        return code;
    }
}
