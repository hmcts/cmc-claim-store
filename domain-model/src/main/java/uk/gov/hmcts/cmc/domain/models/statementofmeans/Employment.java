package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.EachNotNull;
import uk.gov.hmcts.cmc.domain.constraints.ValidEmployment;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
@ValidEmployment
public class Employment {

    @Valid
    @EachNotNull
    private final List<Employer> employers;

    @Valid
    private final SelfEmployment selfEmployment;

    @Valid
    private final Unemployment unemployment;

    public Employment(
        List<Employer> employers,
        SelfEmployment selfEmployment,
        Unemployment unemployment
    ) {
        this.employers = employers;
        this.selfEmployment = selfEmployment;
        this.unemployment = unemployment;
    }

    public List<Employer> getEmployers() {
        return employers == null ? emptyList() : employers;
    }

    public Optional<SelfEmployment> getSelfEmployment() {
        return Optional.ofNullable(selfEmployment);
    }

    public Optional<Unemployment> getUnemployment() {
        return Optional.ofNullable(unemployment);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
