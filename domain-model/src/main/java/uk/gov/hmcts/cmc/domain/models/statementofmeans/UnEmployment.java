package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class UnEmployment {

    @Valid
    private final Unemployed unemployed;
    private final boolean retired;
    private final String other;

    public UnEmployment(Unemployed unemployed, boolean retired, String other) {
        this.unemployed = unemployed;
        this.retired = retired;
        this.other = other;
    }

    public Optional<Unemployed> getUnemployed() {
        return Optional.ofNullable(unemployed);
    }

    public boolean isRetired() {
        return retired;
    }

    public Optional<String> getOther() {
        return Optional.ofNullable(other);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        UnEmployment that = (UnEmployment) other;
        return retired == that.retired
            && Objects.equals(unemployed, that.unemployed)
            && Objects.equals(this.other, that.other);
    }

    @Override
    public int hashCode() {

        return Objects.hash(unemployed, retired, other);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
