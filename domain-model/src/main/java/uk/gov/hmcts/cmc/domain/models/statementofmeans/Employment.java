package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Employment {

    private final YesNoOption isEmployed;
    private final YesNoOption isSelfEmployed;
    private final List<Employer> employers;
    private final SelfEmployed selfEmployed;

    public Employment(
        YesNoOption employed,
        YesNoOption isSelfEmployed,
        List<Employer> employers,
        SelfEmployed selfEmployed
    ) {
        this.isEmployed = employed;
        this.isSelfEmployed = isSelfEmployed;
        this.employers = employers;
        this.selfEmployed = selfEmployed;
    }

    public YesNoOption isEmployed() {
        return isEmployed;
    }

    public YesNoOption isSelfEmployed() {
        return isSelfEmployed;
    }

    public List<Employer> getEmployers() {
        return employers == null ? emptyList() : employers;
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
            && Objects.equals(employers, that.employers)
            && Objects.equals(selfEmployed, that.selfEmployed);
    }

    @Override
    public int hashCode() {

        return Objects.hash(isEmployed, isSelfEmployed, employers, selfEmployed);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
