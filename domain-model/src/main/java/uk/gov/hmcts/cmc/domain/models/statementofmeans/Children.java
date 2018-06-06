package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class Children {

    private final Integer under11;
    private final Integer between11and15;
    private final Integer between16and19;

    public Children(Integer under11, Integer between11and15, Integer between16and19) {
        this.under11 = under11;
        this.between11and15 = between11and15;
        this.between16and19 = between16and19;
    }

    public Optional<Integer> getUnder11() {
        return Optional.ofNullable(under11);
    }

    public Optional<Integer> getBetween11and15() {
        return Optional.ofNullable(between11and15);
    }

    public Optional<Integer> getBetween16and19() {
        return Optional.ofNullable(between16and19);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Children children = (Children) other;
        return Objects.equals(under11, children.under11)
            && Objects.equals(between11and15, children.between11and15)
            && Objects.equals(between16and19, children.between16and19);
    }

    @Override
    public int hashCode() {
        return Objects.hash(under11, between11and15, between16and19);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
