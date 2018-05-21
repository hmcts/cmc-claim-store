package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class Dependant {

    private final Children children;
    private final Integer maintainedChildren;

    public Dependant(Children children, Integer maintainedChildren) {
        this.children = children;
        this.maintainedChildren = maintainedChildren;
    }

    public Optional<Children> getChildren() {
        return Optional.ofNullable(children);
    }

    public Optional<Integer> getMaintainedChildren() {
        return Optional.ofNullable(maintainedChildren);
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
            && Objects.equals(maintainedChildren, dependant.maintainedChildren);
    }

    @Override
    public int hashCode() {

        return Objects.hash(children, maintainedChildren);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
