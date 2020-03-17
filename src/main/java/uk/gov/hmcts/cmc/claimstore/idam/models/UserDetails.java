package uk.gov.hmcts.cmc.claimstore.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@Builder
@EqualsAndHashCode
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

    public Optional<String> getSurname() {
        return Optional.ofNullable(surname);
    }

    @JsonIgnore
    public String getFullName() {
        return getSurname().map(s -> String.join(" ", forename, s))
            .orElse(forename);
    }

    @JsonIgnore
    public boolean isSolicitor() {
        return roles.stream().anyMatch("solicitor"::equals);
    }

    @JsonIgnore
    public boolean isCaseworker() {
        return roles.stream().anyMatch("caseworker-cmc"::equals);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
