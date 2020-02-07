package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class ResponseRejectionAssert extends CustomAssert<ResponseRejectionAssert, ResponseRejection> {

    public ResponseRejectionAssert(ResponseRejection responseRejection) {
        super("ResponseReject", responseRejection, ResponseRejectionAssert.class);
    }

    public ResponseRejectionAssert isEqualTo(CCDResponseRejection expected) {
        isNotNull();

        compare("freeMediation",
            expected.getFreeMediationOption(), CCDYesNoOption::name,
            actual.getFreeMediation().map(YesNoOption::name));

        compare("mediationPhoneNumber",
            expected.getMediationPhoneNumber(), CCDTelephone::getTelephoneNumber,
            actual.getMediationPhoneNumber());

        compare("mediationContactPerson",
            expected.getMediationContactPerson(),
            actual.getMediationContactPerson());

        compare("amountPaid",
            expected.getAmountPaid(),
            actual.getAmountPaid(),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("reason",
            expected.getReason(),
            actual.getReason());

        compare("settleForAmount",
            expected.getSettleForAmount(), CCDYesNoOption::name,
            actual.getSettleForAmount().map(YesNoOption::name));

        compare("paymentReceived",
            expected.getPaymentReceived(), CCDYesNoOption::name,
            actual.getPaymentReceived().map(YesNoOption::name));

        return this;
    }
}
