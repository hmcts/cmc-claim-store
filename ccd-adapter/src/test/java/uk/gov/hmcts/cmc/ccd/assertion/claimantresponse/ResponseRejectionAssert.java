package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import java.util.Objects;

public class ResponseRejectionAssert extends AbstractAssert<ResponseRejectionAssert, ResponseRejection> {
    public ResponseRejectionAssert(ResponseRejection responseRejection) {
        super(responseRejection, ResponseRejectionAssert.class);

    }

    public ResponseRejectionAssert isEqualTo(CCDResponseRejection ccdResponseRejection) {
        isNotNull();

        CCDYesNoOption freeMediation = CCDYesNoOption.valueOf(actual.isFreeMediation());
        if (!Objects.equals(freeMediation, ccdResponseRejection.getFreeMediationOption())) {
            failWithMessage("Expected ResponseRejection.freeMediation to be <%s> but was <%s>",
                ccdResponseRejection.getFreeMediationOption(), freeMediation);
        }

        if (!Objects.equals(actual.getAmountPaid(), ccdResponseRejection.getAmountPaid())) {
            failWithMessage("Expected ResponseRejection.amountPaid to be <%s> but was <%s>",
                ccdResponseRejection.getAmountPaid(), actual.getAmountPaid());
        }

        if (!Objects.equals(actual.getReason(), ccdResponseRejection.getReason())) {
            failWithMessage("Expected ResponseRejection.reason to be <%s> but was <%s>",
                ccdResponseRejection.getReason(), actual.getReason());
        }

        return this;
    }
}
