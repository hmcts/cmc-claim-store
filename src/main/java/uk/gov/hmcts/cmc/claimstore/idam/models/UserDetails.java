package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {

    private final String id;
    private final String email;

    private final String forename;
    private final String surname;

    public UserDetails(@JsonProperty("id") String id,
                       @JsonProperty("email") String email,
                       @JsonProperty("forename") String forename,
                       @JsonProperty("surname") String surname
    ) {
        this.id = id;
        this.email = email;
        this.forename = forename;
        this.surname = surname;
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

    @JsonIgnore
    public String getFullName() {
        return getSurname().map(s -> String.join(" ", forename, s))
            .orElse(forename);
    }
}
