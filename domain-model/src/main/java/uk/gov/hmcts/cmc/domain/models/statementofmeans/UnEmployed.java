package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class UnEmployed {

    private final String type;
    private final Integer noOfYears;
    private final Integer noOfMonths;

    public UnEmployed(String type, Integer noOfYears, Integer noOfMonths) {
        this.type = type;
        this.noOfYears = noOfYears;
        this.noOfMonths = noOfMonths;
    }

    public String getType() {
        return type;
    }

    public Optional<Integer> getNoOfYears() {
        return Optional.ofNullable(noOfYears);
    }

    public Optional<Integer> getNoOfMonths() {
        return Optional.ofNullable(noOfMonths);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        UnEmployed that = (UnEmployed) other;
        return Objects.equals(type, that.type)
            && Objects.equals(noOfYears, that.noOfYears)
            && Objects.equals(noOfMonths, that.noOfMonths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, noOfYears, noOfMonths);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
