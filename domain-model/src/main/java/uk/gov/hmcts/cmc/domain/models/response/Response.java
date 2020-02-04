package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@JsonIgnoreProperties(
    value = "responseType",
    ignoreUnknown = true
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "responseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FullDefenceResponse.class, name = "FULL_DEFENCE"),
    @JsonSubTypes.Type(value = FullAdmissionResponse.class, name = "FULL_ADMISSION"),
    @JsonSubTypes.Type(value = PartAdmissionResponse.class, name = "PART_ADMISSION")
})
@EqualsAndHashCode
public abstract class Response {

    @NotNull
    private final ResponseType responseType;

    private final YesNoOption freeMediation;

    @Size(max = 30, message = "Mediation phone number may not be longer than {max} characters")
    private final String mediationPhoneNumber;

    @Size(max = 30, message = "Mediation contact person may not be longer than {max} characters")
    private final String mediationContactPerson;

    @JsonUnwrapped
    private final YesNoOption moreTimeNeeded;

    @Valid
    @NotNull
    private final Party defendant;

    @Valid
    private final StatementOfTruth statementOfTruth;

    public Response(
        ResponseType responseType,
        YesNoOption freeMediation,
        String mediationPhoneNumber,
        String mediationContactPerson,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth
    ) {
        this.responseType = responseType;
        this.freeMediation = freeMediation;
        this.mediationPhoneNumber = mediationPhoneNumber;
        this.mediationContactPerson = mediationContactPerson;
        this.moreTimeNeeded = moreTimeNeeded;
        this.defendant = defendant;
        this.statementOfTruth = statementOfTruth;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public Optional<YesNoOption> getFreeMediation() {
        return Optional.ofNullable(freeMediation);
    }

    public Optional<String> getMediationPhoneNumber() {
        return Optional.ofNullable(mediationPhoneNumber);
    }

    public Optional<String> getMediationContactPerson() {
        return Optional.ofNullable(mediationContactPerson);
    }

    public YesNoOption getMoreTimeNeeded() {
        return moreTimeNeeded;
    }

    public Party getDefendant() {
        return defendant;
    }

    public Optional<StatementOfTruth> getStatementOfTruth() {
        return Optional.ofNullable(statementOfTruth);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
