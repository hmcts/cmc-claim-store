package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.util.Objects;

public class ResponseRejectionAssert extends AbstractAssert<ResponseRejectionAssert, ResponseRejection> {
    public ResponseRejectionAssert(ResponseRejection responseRejection) {
        super(responseRejection, ResponseRejectionAssert.class);

    }

    public ResponseRejectionAssert isEqualTo(CCDResponseRejection ccdResponseRejection) {
        isNotNull();

        CCDYesNoOption freeMediation = CCDYesNoOption.valueOf((actual.getFreeMediation()
            .orElse(YesNoOption.NO)).name());
        if (!Objects.equals(freeMediation, ccdResponseRejection.getFreeMediationOption())) {
            failWithMessage("Expected ResponseRejection.freeMediation to be <%s> but was <%s>",
                ccdResponseRejection.getFreeMediationOption(), freeMediation);
        }

        actual.getMediationPhoneNumber().ifPresent(mediationPhoneNumber -> {
            if (!Objects.equals(
                mediationPhoneNumber,
                ccdResponseRejection.getMediationPhoneNumber())) {
                failWithMessage("Expected ResponseRejection.mediationPhoneNumber to be <%s> but was <%s>",
                    ccdResponseRejection.getMediationPhoneNumber(), mediationPhoneNumber);
            }
        });

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
