package uk.gov.hmcts.cmc.ccd.util;

import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDStatementType;

import java.time.LocalDate;

public class SampleCCDPartyStatement {

    public static CCDPartyStatement withOffer() {
        return builder().madeBy(CCDMadeBy.CLAIMANT)
            .type(CCDStatementType.ACCEPTATION)
            .offerCompletionDate(LocalDate.now())
            .offerContent("Offer Content")
            .build();
    }

    public static CCDPartyStatement withPaymentIntention() {
        return builder().madeBy(CCDMadeBy.CLAIMANT)
            .type(CCDStatementType.ACCEPTATION)
            .offerCompletionDate(LocalDate.now())
            .offerContent("Offer Content")
            .paymentIntention(SampleCCDPaymentIntention.withInstalment())
            .build();
    }

    private static CCDPartyStatement.CCDPartyStatementBuilder builder() {
        return CCDPartyStatement.builder();
    }
}
