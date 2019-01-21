package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CCDPaidInFullEvent {
    private final String authorization;
    private final Claim claim;
    private final PaidInFull paidInFull;

    public CCDPaidInFullEvent(String authorization, Claim claim, PaidInFull paidInFull) {
        this.authorization = authorization;
        this.claim = claim;
        this.paidInFull = paidInFull;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
