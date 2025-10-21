package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.offers.StatementType;

import java.time.LocalDate;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class PartyStatementMetadata {
    private final StatementType type;
    private final MadeBy madeBy;
    private final LocalDate offerCompletionDate;
    private final PaymentPlanMetadata offerPaymentPlan;

    static PartyStatementMetadata fromPartyStatement(PartyStatement partyStatement) {
        if (partyStatement == null) {
            return null;
        }
        Optional<Offer> optionalOffer = partyStatement.getOffer();
        return new PartyStatementMetadata(
            partyStatement.getType(),
            partyStatement.getMadeBy(),
            optionalOffer
                .map(Offer::getCompletionDate)
                .orElse(null),
            optionalOffer
                .flatMap(Offer::getPaymentIntention)
                .map(PaymentPlanMetadata::fromPaymentIntention)
                .orElse(null)
        );
    }
}
