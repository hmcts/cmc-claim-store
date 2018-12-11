package uk.gov.hmcts.cmc.claimstore.events.settlement;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Getter
@EqualsAndHashCode
public class SignSettlementAgreementEvent {

    private final Claim claim;

    public SignSettlementAgreementEvent(Claim claim) {
        this.claim = claim;
    }
}
