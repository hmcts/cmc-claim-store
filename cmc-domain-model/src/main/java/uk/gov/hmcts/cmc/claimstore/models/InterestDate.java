package uk.gov.hmcts.cmc.claimstore.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.claimstore.constraints.DateNotInTheFuture;
import uk.gov.hmcts.cmc.claimstore.constraints.InterDependentFields;

import java.time.LocalDate;
import java.util.Objects;
import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@InterDependentFields.List({@InterDependentFields(field = "date", dependentField = "type"),
    @InterDependentFields(field = "reason", dependentField = "type")})
public class InterestDate {
    public enum InterestDateType {
        @JsonProperty("custom")
        CUSTOM,

        @JsonProperty("submission")
        SUBMISSION
    }

    @NotNull
    private final InterestDateType type;

    @JsonUnwrapped
    @DateNotInTheFuture
    private final LocalDate date;

    private final String reason;

    public InterestDate(InterestDateType type, LocalDate date, String reason) {
        this.type = type;
        this.date = date;
        this.reason = reason;
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

    @JsonIgnore
    public boolean isValid() {
        return type != null || date != null || reason != null;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final InterestDate that = (InterestDate) other;
        return Objects.equals(type, that.type)
            && Objects.equals(date, that.date)
            && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, date, reason);
    }
}
