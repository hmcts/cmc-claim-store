package uk.gov.hmcts.cmc.claimstore.idam.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class User {
    private String authorisation;
    private UserDetails userDetails;

    public User(String authorisation, UserDetails userDetails) {
        this.authorisation = authorisation;
        this.userDetails = userDetails;
    }

    public String getAuthorisation() {
        return authorisation;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
