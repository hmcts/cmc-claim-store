package uk.gov.hmcts.cmc.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.constraints.DateNotInTheFuture;
import uk.gov.hmcts.cmc.domain.constraints.InterDependentFields;

import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;

@InterDependentFields.List({@InterDependentFields(field = "date", dependentField = "type"),
    @InterDependentFields(field = "reason", dependentField = "type")})
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

    private final InterestEndDateType endDate;

    public InterestDate(InterestDateType type, LocalDate date, String reason, InterestEndDateType endDate) {
        this.type = type;
        this.date = date;
        this.reason = reason;
        this.endDate = endDate == null ? InterestEndDateType.SUBMISSION : endDate;
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

    public InterestEndDateType getEndDate() { return endDate; }

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
            && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, date, reason);
    }
}
