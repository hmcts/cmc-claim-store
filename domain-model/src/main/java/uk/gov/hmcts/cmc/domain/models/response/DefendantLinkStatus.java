package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class DefendantLinkStatus {

    private final boolean linked;

    @JsonCreator
    public DefendantLinkStatus(boolean linked) {
        this.linked = linked;
    }

    public boolean isLinked() {
        return linked;
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
        return linked == status1.linked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(linked);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
