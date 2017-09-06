package uk.gov.hmcts.cmc.claimstore.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;

import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ResponseData {

    public enum ResponseType {
        OWE_ALL_PAID_ALL,
        OWE_ALL_PAID_NONE,
        OWE_SOME_PAID_NONE,
        OWE_ALL_PAID_SOME,
        OWE_NONE
    }

    public enum FreeMediationOption {
        @JsonProperty("yes")
        YES,
        @JsonProperty("no")
        NO
    }

    public enum MoreTimeNeededOption {
        @JsonProperty("yes")
        YES,
        @JsonProperty("no")
        NO
    }

    @NotNull
    @JsonUnwrapped
    @JsonProperty("response")
    private final ResponseType type;

    @NotBlank
    @Size(max = 99000)
    private final String defence;

    @NotNull
    @JsonUnwrapped
    private final FreeMediationOption freeMediation;

    @JsonUnwrapped
    private final MoreTimeNeededOption moreTimeNeeded;

    @Valid
    @NotNull
    private final Party defendant;

    public ResponseData(final ResponseType type,
                        final String defence,
                        final FreeMediationOption freeMediation,
                        final MoreTimeNeededOption moreTimeNeeded,
                        final Party defendant) {
        this.type = type;
        this.defence = defence;
        this.freeMediation = freeMediation;
        this.moreTimeNeeded = moreTimeNeeded;
        this.defendant = defendant;
    }

    public ResponseType getType() {
        return type;
    }

    public String getDefence() {
        return defence;
    }

    public FreeMediationOption getFreeMediation() {
        return freeMediation;
    }

    public MoreTimeNeededOption getMoreTimeNeeded() {
        return moreTimeNeeded;
    }

    public Party getDefendant() {
        return defendant;
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final ResponseData that = (ResponseData) other;
        return Objects.equals(type, that.type)
            && Objects.equals(defence, that.defence)
            && Objects.equals(freeMediation, that.freeMediation)
            && Objects.equals(moreTimeNeeded, that.moreTimeNeeded)
            && Objects.equals(defendant, that.defendant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, defence, freeMediation, moreTimeNeeded, defendant);
    }

    @Override
    public String toString() {
        return String.format(
            "ResponseData{type=%s, defence='%s', freeMediation=%s, moreTimeNeeded=%s, defendant=%s}",
            type, defence, freeMediation, moreTimeNeeded, defendant
        );
    }
}
