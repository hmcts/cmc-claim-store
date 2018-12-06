package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

import java.util.Optional;

public abstract class FullDefenceResponseMixIn extends ResponseMixIn {

    @JsonProperty("responseDefenceType")
    abstract DefenceType getDefenceType();

    @JsonProperty("responseDefence")
    abstract Optional<String> getDefence();

    @JsonUnwrapped
    abstract Optional<PaymentDeclaration> getPaymentDeclaration();

    @JsonUnwrapped
    abstract Optional<DefendantTimeline> getTimeline();

    @JsonUnwrapped
    abstract Optional<DefendantEvidence> getEvidence();
}
