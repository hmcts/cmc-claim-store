package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Dependant dependant = (Dependant) other;
        return Objects.equals(children, dependant.children)
            && Objects.equals(numberOfMaintainedChildren, dependant.numberOfMaintainedChildren)
            && Objects.equals(otherDependants, dependant.otherDependants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, numberOfMaintainedChildren, otherDependants);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
