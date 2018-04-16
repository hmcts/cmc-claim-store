package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticateUserResponse {
    @JsonProperty("default-url")
    private String defaultUser;
    private String code;
    @JsonProperty("access-token")
    private String accessToken;

    public AuthenticateUserResponse(String defaultUser, String code, String accessToken) {
        this.defaultUser = defaultUser;
        this.code = code;
        this.accessToken = accessToken;
    }

    public String getDefaultUser() {
        return defaultUser;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getCode() {
        return code;
    }
}
