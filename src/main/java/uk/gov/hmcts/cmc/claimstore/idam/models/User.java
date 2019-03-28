package uk.gov.hmcts.cmc.claimstore.idam.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class User {
    private final String authorisation;
    private final UserDetails userDetails;

    public User(String authorisation, UserDetails userDetails) {
        this.authorisation = authorisation;
        this.userDetails = userDetails;
    }

    public boolean isRepresented() {
        return this.getUserDetails().isSolicitor() || this.getUserDetails().isCaseworker();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
