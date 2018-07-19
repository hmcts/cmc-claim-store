package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
public class ClaimState {

    private final boolean isOnHold;

    @JsonCreator
    public ClaimState(boolean isOnHold) {
        this.isOnHold = isOnHold;
    }

    public boolean isOnHold() {
        return isOnHold;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
