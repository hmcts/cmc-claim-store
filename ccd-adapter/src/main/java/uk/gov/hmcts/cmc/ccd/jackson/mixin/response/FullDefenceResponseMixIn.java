package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

public abstract class FullDefenceResponseMixIn extends ResponseMixIn {

    @JsonProperty("responseDefenceType")
    abstract DefenceType getDefenceType();

    @JsonProperty("responseDefence")
    abstract String getDefence();

    @JsonUnwrapped
    abstract PaymentDeclaration getPaymentDeclaration();

    @JsonProperty("defendantTimeLineEvents")
    abstract DefendantTimeline getTimeline();

    @JsonProperty("responseEvidenceRows")
    abstract DefendantEvidence getEvidence();
}
