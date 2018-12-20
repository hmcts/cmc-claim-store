package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.offers.PartyStatement;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;


public class Assertions {

    private Assertions() {
    }

    public static PartyStatementAssert assertThat(PartyStatement partyStatement) {
        return new PartyStatementAssert(partyStatement);
    }

    public static PaymentIntentionAssert assertThat(PaymentIntention paymentIntention) {
        return new PaymentIntentionAssert(paymentIntention);
    }

}
