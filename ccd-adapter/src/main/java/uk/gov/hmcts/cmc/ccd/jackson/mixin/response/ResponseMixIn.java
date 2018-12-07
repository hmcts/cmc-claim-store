package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Optional;

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
