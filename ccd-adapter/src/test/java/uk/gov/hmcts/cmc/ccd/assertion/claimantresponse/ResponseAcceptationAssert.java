package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import java.util.Objects;

public class ResponseAcceptationAssert extends AbstractAssert<ResponseAcceptationAssert, ResponseAcceptation> {
    public ResponseAcceptationAssert(ResponseAcceptation responseAcceptation) {
        super(responseAcceptation, ResponseAcceptationAssert.class);
    }

    public ResponseAcceptationAssert isEqualTo(CCDResponseAcceptation ccdResponseAcceptation) {
        isNotNull();

        if (!Objects.equals(
            actual.getFormaliseOption().orElseThrow(AssertionError::new).name(),
            ccdResponseAcceptation.getFormaliseOption().name()
        )) {
            failWithMessage("Expected ResponseAcceptation.formaliseOption to be <%s> but was <%s>",
                ccdResponseAcceptation.getFormaliseOption(), actual.getFormaliseOption());
        }

        actual.getAmountPaid().ifPresent(amountPaid -> {
            if (!Objects.equals(amountPaid, ccdResponseAcceptation.getAmountPaid())) {
                failWithMessage("Expected ResponseAcceptation.amountPaid to be <%s> but was <%s>",
                    ccdResponseAcceptation.getAmountPaid(), amountPaid);
            }
        });

        return this;
    }
}
