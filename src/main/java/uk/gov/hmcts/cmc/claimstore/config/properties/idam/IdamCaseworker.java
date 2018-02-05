package uk.gov.hmcts.cmc.claimstore.config.properties.idam;

import org.hibernate.validator.constraints.NotBlank;

public class IdamCaseworker {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
