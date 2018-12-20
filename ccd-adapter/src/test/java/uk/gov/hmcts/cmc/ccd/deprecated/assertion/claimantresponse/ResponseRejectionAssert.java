package uk.gov.hmcts.cmc.ccd.deprecated.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import java.util.Objects;

public class ResponseRejectionAssert extends AbstractAssert<ResponseRejectionAssert, ResponseRejection> {
    public ResponseRejectionAssert(ResponseRejection responseRejection) {
        super(responseRejection, ResponseRejectionAssert.class);

    }

    public ResponseRejectionAssert isEqualTo(CCDResponseRejection ccdResponseRejection) {
        isNotNull();

        CCDYesNoOption freeMediation = CCDYesNoOption.valueOf(actual.getFreeMediation().orElse(false));
        if (!Objects.equals(freeMediation, ccdResponseRejection.getFreeMediationOption())) {
            failWithMessage("Expected ResponseRejection.freeMediation to be <%s> but was <%s>",
                ccdResponseRejection.getFreeMediationOption(), freeMediation);
        }

        actual.getAmountPaid().ifPresent(amountPaid -> {
            if (!Objects.equals(amountPaid, ccdResponseRejection.getAmountPaid())) {
                failWithMessage("Expected ResponseRejection.amountPaid to be <%s> but was <%s>",
                    ccdResponseRejection.getAmountPaid(), amountPaid);
            }
        });

        actual.getReason().ifPresent(reason -> {
            if (!Objects.equals(reason, ccdResponseRejection.getReason())) {
                failWithMessage("Expected ResponseRejection.reason to be <%s> but was <%s>",
                    ccdResponseRejection.getReason(), reason);
            }
        });

        return this;
    }
}
