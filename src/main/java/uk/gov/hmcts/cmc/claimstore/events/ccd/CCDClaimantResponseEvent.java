package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@Getter
public class CCDClaimantResponseEvent {
    private final Claim claim;
    private final ClaimantResponse response;
    private final String authorization;

    public CCDClaimantResponseEvent(Claim claim, ClaimantResponse response, String authorization) {

        this.claim = claim;
        this.response = response;
        this.authorization = authorization;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
