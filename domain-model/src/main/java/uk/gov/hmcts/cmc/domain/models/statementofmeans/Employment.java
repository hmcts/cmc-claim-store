package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class Employment {

    private final YesNoOption isCurrentlyEmployed;
    private final boolean employed;
    private final boolean selfEmployed;

    public Employment(YesNoOption isCurrentlyEmployed, boolean employed, boolean selfEmployed) {
        this.isCurrentlyEmployed = isCurrentlyEmployed;
        this.employed = employed;
        this.selfEmployed = selfEmployed;
    }

    public YesNoOption getIsCurrentlyEmployed() {
        return isCurrentlyEmployed;
    }

    public boolean isEmployed() {
        return employed;
    }

    public boolean isSelfEmployed() {
        return selfEmployed;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Employment that = (Employment) other;
        return employed == that.employed
            && selfEmployed == that.selfEmployed
            && isCurrentlyEmployed == that.isCurrentlyEmployed;
    }

    @Override
    public int hashCode() {

        return Objects.hash(isCurrentlyEmployed, employed, selfEmployed);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
