package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "responseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FullDefenceResponse.class, name = "FULL_DEFENCE"),
    @JsonSubTypes.Type(value = FullAdmissionResponse.class, name = "FULL_ADMISSION"),
    @JsonSubTypes.Type(value = PartAdmissionResponse.class, name = "PART_ADMISSION")
})
public interface ResponseMixIn {

    @JsonProperty("responseType")
    ResponseType getResponseType();

    @JsonProperty("responseFreeMediationOption")
    Optional<YesNoOption> getFreeMediation();

    @JsonProperty("responseMoreTimeNeededOption")
    YesNoOption getMoreTimeNeeded();

    @JsonUnwrapped
    Party getDefendant();

    @JsonUnwrapped
    Optional<StatementOfTruth> getStatementOfTruth();

}
