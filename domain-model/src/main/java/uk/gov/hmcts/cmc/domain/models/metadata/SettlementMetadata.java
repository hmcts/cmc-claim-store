package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.util.List;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class SettlementMetadata {
    private final List<PartyStatement> partyStatements;

    static SettlementMetadata fromClaim(Claim claim) {
        final Optional<Settlement> optionalSettlement = claim.getSettlement();
        if (!optionalSettlement.isPresent()) {
            return null;
        }
        final Settlement settlement = optionalSettlement.get();
        return new SettlementMetadata(
            settlement.getPartyStatements()
        );
    }
}
