package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class Employment {

    private final YesNoOption isCurrentlyEmployed;
    private final boolean isEmployed;
    private final boolean isSelfEmployed;
    private final List<Employer> employers;
    private final SelfEmployed selfEmployed;

    public Employment(
        YesNoOption isCurrentlyEmployed,
        boolean employed,
        boolean isSelfEmployed,
        List<Employer> employers,
        SelfEmployed selfEmployed
    ) {
        this.isCurrentlyEmployed = isCurrentlyEmployed;
        this.isEmployed = employed;
        this.isSelfEmployed = isSelfEmployed;
        this.employers = employers;
        this.selfEmployed = selfEmployed;
    }

    public YesNoOption getIsCurrentlyEmployed() {
        return isCurrentlyEmployed;
    }

    public boolean isEmployed() {
        return isEmployed;
    }

    public boolean isSelfEmployed() {
        return isSelfEmployed;
    }

    public List<Employer> getEmployers() {
        return employers;
    }

    public Optional<SelfEmployed> getSelfEmployed() {
        return Optional.ofNullable(selfEmployed);
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
        return isEmployed == that.isEmployed
            && isSelfEmployed == that.isSelfEmployed
            && isCurrentlyEmployed == that.isCurrentlyEmployed
            && Objects.equals(employers, that.employers)
            && Objects.equals(selfEmployed, that.selfEmployed);
    }

    @Override
    public int hashCode() {

        return Objects.hash(isCurrentlyEmployed, isEmployed, isSelfEmployed, employers, selfEmployed);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
