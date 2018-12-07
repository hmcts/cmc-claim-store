package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer.DefendantTimelineDeserializer;
import uk.gov.hmcts.cmc.ccd.jackson.custom.serializer.DefendantTimelineSerializer;
import uk.gov.hmcts.cmc.domain.models.PaymentDeclaration;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;
import uk.gov.hmcts.cmc.domain.models.response.DefenceType;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

import java.util.Optional;

public interface FullDefenceResponseMixIn extends ResponseMixIn {

    @JsonProperty("responseDefenceType")
    DefenceType getDefenceType();

    @JsonProperty("responseDefence")
    Optional<String> getDefence();

    @JsonUnwrapped
    Optional<PaymentDeclaration> getPaymentDeclaration();


//    @JsonDeserialize(using = DefendantTimelineDeserializer.class)
    @JsonSerialize(using = DefendantTimelineSerializer.class)
    @JsonUnwrapped
    Optional<DefendantTimeline> getTimeline();

//    @JsonUnwrapped
    @JsonIgnore
    Optional<DefendantEvidence> getEvidence();
}
