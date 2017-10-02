package uk.gov.hmcts.cmc.claimstore.models;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;

import static uk.gov.hmcts.cmc.claimstore.utils.ToStringStyle.ourStyle;

public class DefendantLinkStatus {

    private final boolean status;

    public DefendantLinkStatus(final boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DefendantLinkStatus status1 = (DefendantLinkStatus) other;
        return status == status1.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
