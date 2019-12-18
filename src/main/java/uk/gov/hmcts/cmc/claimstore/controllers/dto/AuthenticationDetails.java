package uk.gov.hmcts.cmc.claimstore.controllers.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.utils.ToStringStyle;

@EqualsAndHashCode
@Getter
@Setter
public class AuthenticationDetails {

    private String username;
    private String password;

    @Builder
    @JsonCreator
    public AuthenticationDetails(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.ourStyle());
    }

}
