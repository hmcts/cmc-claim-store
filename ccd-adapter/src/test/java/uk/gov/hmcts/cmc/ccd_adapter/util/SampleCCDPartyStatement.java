package uk.gov.hmcts.cmc.ccd_adapter.util;

import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDPartyStatement;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDMadeBy;
import uk.gov.hmcts.cmc.ccd.domain.offers.CCDStatementType;

import java.time.LocalDate;

public class SampleCCDPartyStatement {

    private SampleCCDPartyStatement() {
        // Empty constructor
    }

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

    public static CCDPartyStatement offerPartyStatement() {
        return CCDPartyStatement.builder().madeBy(CCDMadeBy.DEFENDANT)
            .offerContent("Will pay you soon")
            .offerCompletionDate(LocalDate.now().plusMonths(6L))
            .type(CCDStatementType.OFFER)
            .paymentIntention(SampleCCDPaymentIntention.withInstalment())
            .build();
    }

    public static CCDPartyStatement rejectPartyStatement() {
        return CCDPartyStatement.builder().madeBy(CCDMadeBy.CLAIMANT)
            .offerContent("I got no money mate")
            .type(CCDStatementType.REJECTION)
            .build();
    }

    public static CCDPartyStatement acceptPartyStatement() {
        return CCDPartyStatement.builder().madeBy(CCDMadeBy.CLAIMANT)
            .offerContent("Cheers.")
            .type(CCDStatementType.ACCEPTATION)
            .build();
    }

    public static CCDPartyStatement counterSignPartyStatement() {
        return CCDPartyStatement.builder().madeBy(CCDMadeBy.DEFENDANT)
            .offerContent("Forgot my pen, will sign soon")
            .type(CCDStatementType.COUNTERSIGNATURE)
            .build();
    }

    private static CCDPartyStatement.CCDPartyStatementBuilder builder() {
        return CCDPartyStatement.builder();
    }
}
