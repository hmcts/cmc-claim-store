package uk.gov.hmcts.cmc.domain.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;

import java.util.Optional;

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
    protected final ResponseType responseType;

    protected ResponseMethod responseMethod;

    protected final YesNoOption freeMediation;

    @Size(max = 30, message = "Mediation phone number may not be longer than {max} characters")
    protected final String mediationPhoneNumber;

    @Size(max = 30, message = "Mediation contact person may not be longer than {max} characters")
    protected final String mediationContactPerson;

    @Size(max = 550, message = "No mediation reason may not be longer than {max} characters")
    protected final String noMediationReason;

    @JsonUnwrapped
    protected final YesNoOption moreTimeNeeded;

    @Valid
    @NotNull
    protected final Party defendant;

    @Valid
    protected final StatementOfTruth statementOfTruth;

    public Response(
        ResponseType responseType,
        YesNoOption freeMediation,
        String mediationPhoneNumber,
        String mediationContactPerson,
        String noMediationReason,
        YesNoOption moreTimeNeeded,
        Party defendant,
        StatementOfTruth statementOfTruth,
        ResponseMethod responseMethod
    ) {
        this.responseType = responseType;
        this.freeMediation = freeMediation;
        this.mediationPhoneNumber = mediationPhoneNumber;
        this.mediationContactPerson = mediationContactPerson;
        this.noMediationReason = noMediationReason;
        this.moreTimeNeeded = moreTimeNeeded;
        this.defendant = defendant;
        this.statementOfTruth = statementOfTruth;
        this.responseMethod = responseMethod;
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

    public Optional<String> getNoMediationReason() {
        return Optional.ofNullable(noMediationReason);
    }

    public YesNoOption getMoreTimeNeeded() {
        return moreTimeNeeded;
    }

    public Optional<ResponseMethod> getResponseMethod() {
        return Optional.ofNullable(responseMethod);
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
