package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateUserResponse {

    private String code;

    public AuthenticateUserResponse(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
