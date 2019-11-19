package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ResponseAcceptationAssert extends CustomAssert<ResponseAcceptationAssert, ResponseAcceptation> {
    public ResponseAcceptationAssert(ResponseAcceptation responseAcceptation) {
        super("ResponseAcceptation", responseAcceptation, ResponseAcceptationAssert.class);
    }

    public ResponseAcceptationAssert isEqualTo(CCDResponseAcceptation expected) {
        isNotNull();

        compare("formaliseOption",
            expected.getFormaliseOption(), CCDFormaliseOption::name,
            actual.getFormaliseOption().map(FormaliseOption::name));

        compare("amountPaid",
            expected.getAmountPaid(),
            actual.getAmountPaid(),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("courtDetermination",
            expected.getCourtDetermination(),
            actual.getCourtDetermination(),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("claimantPaymentIntention",
            expected.getClaimantPaymentIntention(),
            actual.getClaimantPaymentIntention(),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("settleForAmount",
            expected.getSettleForAmount(), CCDYesNoOption::name,
            actual.getSettleForAmount().map(YesNoOption::name));

        compare("paymentReceived",
            expected.getPaymentReceived(), CCDYesNoOption::name,
            actual.getPaymentReceived().map(YesNoOption::name));

        return this;
    }
}
