package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;


public class ResponseAcceptationAssert extends AbstractAssert<ResponseAcceptationAssert, ResponseAcceptation> {
    public ResponseAcceptationAssert(ResponseAcceptation actual) {
        super(actual, ResponseAcceptationAssert.class);
    }

    public ResponseAcceptationAssert isEqualTo(CCDResponseAcceptation ccdResponseAcceptation) {
        isNotNull();

        actual.getFormaliseOption().ifPresent(formaliseOption -> {
            if (!Objects.equals(formaliseOption.name(), ccdResponseAcceptation.getFormaliseOption().name()
            )) {
                failWithMessage("Expected ResponseAcceptation.formaliseOption to be <%s> but was <%s>",
                    ccdResponseAcceptation.getFormaliseOption(), actual.getFormaliseOption());
            }
        });

        actual.getAmountPaid().ifPresent(amountPaid -> {
            if (!Objects.equals(amountPaid, ccdResponseAcceptation.getAmountPaid())) {
                failWithMessage("Expected ResponseAcceptation.amountPaid to be <%s> but was <%s>",
                    ccdResponseAcceptation.getAmountPaid(), amountPaid);
            }
        });

        actual.getClaimantPaymentIntention().ifPresent(paymentIntention ->
            assertThat(paymentIntention).isEqualTo(ccdResponseAcceptation.getClaimantPaymentIntention()));

        return this;
    }
}
