package uk.gov.hmcts.cmc.claimstore.idam.models;

import java.util.List;

public class GeneratePinRequest {

    public final String firstName;
    public final String lastName;
    public final List<String> roles;

    public GeneratePinRequest(String name) {
        this.firstName = name;
        this.lastName = "";
        this.roles = null;
    }
}

