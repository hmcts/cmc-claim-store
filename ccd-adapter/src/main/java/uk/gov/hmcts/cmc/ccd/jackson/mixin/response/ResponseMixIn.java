package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

public abstract class ResponseMixIn {

    @JsonProperty("responseType")
    abstract ResponseType getResponseType();

    @JsonProperty("responseFreeMediationOption")
    abstract YesNoOption getFreeMediation();

    @JsonProperty("responseMoreTimeNeededOption")
    abstract YesNoOption getMoreTimeNeeded();

    @JsonUnwrapped
    abstract Party getDdefendant();

    @JsonUnwrapped
    abstract StatementOfTruth getStatementOfTruth();

}
