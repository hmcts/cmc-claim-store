package uk.gov.hmcts.cmc.ccd.domain.claimantresponse;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.EXISTING_PROPERTY,property = "claimantResponseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CCDResponseAcceptation.class, name = "ACCEPTATION"),
    @JsonSubTypes.Type(value = CCDResponseRejection.class, name = "REJECTION")
})
public abstract class CCDClaimantResponse {
    public abstract CCDClaimantResponseType getClaimantResponseType();
}
