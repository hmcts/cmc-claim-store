package uk.gov.hmcts.cmc.ccd.migration.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {

    private final String id;
    private final String email;

    private final String forename;
    private final String surname;
    private final List<String> roles;

    public UserDetails(
        String id,
        String email,
        String forename,
        String surname,
        List<String> roles
    ) {
        this.id = id;
        this.email = email;
        this.forename = forename;
        this.surname = surname;
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getForename() {
        return forename;
    }

    public Optional<String> getSurname() {
        return Optional.ofNullable(surname);
    }

    public List<String> getRoles() {
        return roles;
    }

    @JsonIgnore
    public String getFullName() {
        return getSurname().map(s -> String.join(" ", forename, s))
            .orElse(forename);
    }
}
