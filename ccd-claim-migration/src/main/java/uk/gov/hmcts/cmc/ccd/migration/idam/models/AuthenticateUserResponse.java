package uk.gov.hmcts.cmc.ccd.migration.idam.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticateUserResponse {
    @JsonProperty("default-url")
    private String defaultUser;
    @JsonProperty("access-token")
    private String accessToken;

    public AuthenticateUserResponse(String defaultUser, String accessToken) {
        this.defaultUser = defaultUser;
        this.accessToken = accessToken;
    }

    public String getDefaultUser() {
        return defaultUser;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
