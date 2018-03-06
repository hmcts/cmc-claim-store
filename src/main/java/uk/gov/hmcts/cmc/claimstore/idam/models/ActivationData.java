package uk.gov.hmcts.cmc.claimstore.idam.models;

public class ActivationData {
    private String password;

    public ActivationData(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
