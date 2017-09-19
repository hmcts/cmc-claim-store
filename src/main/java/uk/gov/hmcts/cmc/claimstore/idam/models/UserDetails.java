package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {

    private final long id;
    private final String email;

    private final String forename;
    private final String surname;

    public UserDetails(final long id,
                       final String email,
                       final String forename,
                       final String surname
    ) {
        this.id = id;
        this.email = email;
        this.forename = forename;
        this.surname = surname;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getForename() {
        return forename;
    }

    public String getSurname() {
        return surname;
    }

    public String getFullName() {
        return forename + ' ' + surname;
    }
}
