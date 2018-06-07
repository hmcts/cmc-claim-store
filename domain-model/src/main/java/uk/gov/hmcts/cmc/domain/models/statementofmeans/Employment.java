package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.EachNotNull;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Employment {

    @NotNull
    private final YesNoOption employmentOption;

    @NotNull
    private final YesNoOption selfEmployedOption;

    @Valid
    @EachNotNull
    private final List<Employer> employers;

    @Valid
    private final SelfEmployed selfEmployed;

    @Valid
    private final UnEmployed unEmployed;

    public Employment(
        YesNoOption employed,
        YesNoOption selfEmployedOption,
        List<Employer> employers,
        SelfEmployed selfEmployed,
        UnEmployed unEmployed
    ) {
        this.employmentOption = employed;
        this.selfEmployedOption = selfEmployedOption;
        this.employers = employers;
        this.selfEmployed = selfEmployed;
        this.unEmployed = unEmployed;
    }

    public YesNoOption getEmploymentOption() {
        return employmentOption;
    }

    public YesNoOption getSelfEmployedOption() {
        return selfEmployedOption;
    }

    public List<Employer> getEmployers() {
        return employers == null ? emptyList() : employers;
    }

    public Optional<SelfEmployed> getSelfEmployed() {
        return Optional.ofNullable(selfEmployed);
    }

    public Optional<UnEmployed> getUnEmployed() {
        return Optional.ofNullable(unEmployed);
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
        return employmentOption == that.employmentOption
            && selfEmployedOption == that.selfEmployedOption
            && Objects.equals(employers, that.employers)
            && Objects.equals(selfEmployed, that.selfEmployed)
            && Objects.equals(unEmployed, that.unEmployed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employmentOption, selfEmployedOption, employers, selfEmployed, unEmployed);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
