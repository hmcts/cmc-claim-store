package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponseType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "claimantResponseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ResponseAcceptation.class, name = "ACCEPTATION"),
    @JsonSubTypes.Type(value = ResponseRejection.class, name = "REJECTION")
})
public abstract class ClaimantResponseMixin {

    @JsonIgnore
    public abstract ClaimantResponseType getType();
}
