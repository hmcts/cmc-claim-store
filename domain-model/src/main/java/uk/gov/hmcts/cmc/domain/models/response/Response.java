package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "responseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FullDefenceResponse.class, name = "FULL_DEFENCE")
})
public abstract class Response {

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

    private final FreeMediationOption freeMediation;

    @JsonUnwrapped
    private final MoreTimeNeededOption moreTimeNeeded;

    @Valid
    @NotNull
    private final Party defendant;

    @Valid
    private final StatementOfTruth statementOfTruth;

    public Response(
            FreeMediationOption freeMediation,
            MoreTimeNeededOption moreTimeNeeded,
            Party defendant,
            StatementOfTruth statementOfTruth
    ) {
        this.freeMediation = freeMediation;
        this.moreTimeNeeded = moreTimeNeeded;
        this.defendant = defendant;
        this.statementOfTruth = statementOfTruth;
    }

    public Optional<FreeMediationOption> getFreeMediation() {
        return Optional.ofNullable(freeMediation);
    }

    public MoreTimeNeededOption getMoreTimeNeeded() {
        return moreTimeNeeded;
    }

    public Party getDefendant() {
        return defendant;
    }

    public Optional<StatementOfTruth> getStatementOfTruth() {
        return Optional.ofNullable(statementOfTruth);
    }

    @Override
    @SuppressWarnings("squid:S1067") // Its generated code for equals sonar
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Response that = (Response) other;
        return Objects.equals(freeMediation, that.freeMediation)
            && Objects.equals(moreTimeNeeded, that.moreTimeNeeded)
            && Objects.equals(defendant, that.defendant)
            && Objects.equals(statementOfTruth, that.statementOfTruth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(freeMediation, moreTimeNeeded, defendant);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
