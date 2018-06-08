package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Dependant {

    @Valid
    private final Children children;

    @Valid
    private final OtherDependants otherDependants;

    public Dependant(Children children, OtherDependants otherDependants) {
        this.children = children;
        this.otherDependants = otherDependants;
    }

    public Optional<Children> getChildren() {
        return Optional.ofNullable(children);
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
            && Objects.equals(otherDependants, dependant.otherDependants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children, otherDependants);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
