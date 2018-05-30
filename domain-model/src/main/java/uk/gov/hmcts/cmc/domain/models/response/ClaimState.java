package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ClaimState status1 = (ClaimState) other;

        return isOnHold == status1.isOnHold;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOnHold);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
