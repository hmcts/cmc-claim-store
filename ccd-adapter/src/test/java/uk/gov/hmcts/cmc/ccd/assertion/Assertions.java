package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.CourtDeterminationAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ResponseAcceptationAssert;
import uk.gov.hmcts.cmc.ccd.assertion.claimantresponse.ResponseRejectionAssert;
import uk.gov.hmcts.cmc.ccd.assertion.response.PaymentIntentionAssert;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;


public class Assertions {

    private Assertions() {
    }

    public static PaymentIntentionAssert assertThat(PaymentIntention paymentIntention) {
        return new PaymentIntentionAssert(paymentIntention);
    }

    public static CourtDeterminationAssert assertThat(CourtDetermination courtDetermination) {
        return new CourtDeterminationAssert(courtDetermination);
    }

    public static ResponseRejectionAssert assertThat(ResponseRejection responseRejection) {
        return new ResponseRejectionAssert(responseRejection);
    }

    public static ResponseAcceptationAssert assertThat(ResponseAcceptation responseAcceptation) {
        return new ResponseAcceptationAssert(responseAcceptation);
    }
}
