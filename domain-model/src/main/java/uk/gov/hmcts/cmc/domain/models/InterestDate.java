package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;

import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

public class InterestDate {
    public enum InterestDateType {
        @JsonProperty("custom")
        CUSTOM,

        @JsonProperty("submission")
        SUBMISSION
    }

    public enum InterestEndDateType {
        @JsonProperty("settled_or_judgment")
        SETTLED_OR_JUDGMENT,

        @JsonProperty("submission")
        SUBMISSION
    }

    @NotNull
    private final InterestDateType type;

    @JsonUnwrapped
    @DateNotInTheFuture
    private final LocalDate date;

    private final String reason;

    private final InterestEndDateType endDateType;

    public InterestDate(InterestDateType type, LocalDate date, String reason, InterestEndDateType endDateType) {
        this.type = type;
        this.date = date;
        this.reason = reason;
        this.endDateType = endDateType == null ? InterestEndDateType.SETTLED_OR_JUDGMENT : endDateType;
    }

    public InterestDateType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getReason() {
        return reason;
    }

    public InterestEndDateType getEndDateType() {
        return endDateType;
    }

    @JsonIgnore
    public boolean isEndDateOnClaimComplete() {
        return endDateType.equals(InterestEndDateType.SETTLED_OR_JUDGMENT);
    }

    @JsonIgnore
    public boolean isEndDateOnSubmission() {
        return endDateType.equals(InterestEndDateType.SUBMISSION);
    }

    @JsonIgnore
    public boolean isCustom() {
        return type.equals(InterestDate.InterestDateType.CUSTOM);
    }

    @JsonIgnore
    public boolean isValid() {
        return type != null || date != null || reason != null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        InterestDate that = (InterestDate) other;
        return Objects.equals(type, that.type)
            && Objects.equals(date, that.date)
            && Objects.equals(reason, that.reason)
            && Objects.equals(endDateType, that.endDateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, date, reason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
