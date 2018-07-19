package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class Dependant {

    @Valid
    private final List<Child> children;

    @Min(0)
    private final Integer numberOfMaintainedChildren;

    @Valid
    private final OtherDependants otherDependants;

    public Dependant(
        List<Child> children,
        Integer numberOfMaintainedChildren,
        OtherDependants otherDependants) {
        this.children = children;
        this.numberOfMaintainedChildren = numberOfMaintainedChildren;
        this.otherDependants = otherDependants;
    }

    public List<Child> getChildren() {
        return children != null ? children : emptyList();
    }

    public Optional<Integer> getNumberOfMaintainedChildren() {
        return Optional.ofNullable(numberOfMaintainedChildren);
    }

    public Optional<OtherDependants> getOtherDependants() {
        return Optional.ofNullable(otherDependants);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
