package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.EachNotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Employment {

    @Valid
    @EachNotNull
    private final List<Employer> employers;

    @Valid
    private final SelfEmployment selfEmployment;

    @Valid
    private final UnEmployment unEmployment;

    public Employment(
        List<Employer> employers,
        SelfEmployment selfEmployment,
        UnEmployment unEmployment
    ) {
        this.employers = employers;
        this.selfEmployment = selfEmployment;
        this.unEmployment = unEmployment;
    }

    public List<Employer> getEmployers() {
        return employers == null ? emptyList() : employers;
    }

    public Optional<SelfEmployment> getSelfEmployment() {
        return Optional.ofNullable(selfEmployment);
    }

    public Optional<UnEmployment> getUnEmployment() {
        return Optional.ofNullable(unEmployment);
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
        return Objects.equals(employers, that.employers)
            && Objects.equals(selfEmployment, that.selfEmployment)
            && Objects.equals(unEmployment, that.unEmployment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employers, selfEmployment, unEmployment);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
