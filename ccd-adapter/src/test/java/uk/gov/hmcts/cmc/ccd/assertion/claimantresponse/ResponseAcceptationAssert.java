package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ResponseAcceptationAssert extends AbstractAssert<ResponseAcceptationAssert, ResponseAcceptation> {
    public ResponseAcceptationAssert(ResponseAcceptation responseAcceptation) {
        super(responseAcceptation, ResponseAcceptationAssert.class);
    }

    public ResponseAcceptationAssert isEqualTo(CCDResponseAcceptation ccdResponseAcceptation) {
        isNotNull();

        if (!Objects.equals(actual.getFormaliseOption().name(), ccdResponseAcceptation.getFormaliseOption().name())) {
            failWithMessage("Expected ResponseAcceptation.formaliseOption to be <%s> but was <%s>",
                ccdResponseAcceptation.getFormaliseOption(), actual.getFormaliseOption());
        }

        actual.getAmountPaid().ifPresent(amountPaid -> {
            if (!Objects.equals(amountPaid, ccdResponseAcceptation.getAmountPaid())) {
                failWithMessage("Expected ResponseAcceptation.amountPaid to be <%s> but was <%s>",
                    ccdResponseAcceptation.getAmountPaid(), amountPaid);
            }
        });

        actual.getCourtDetermination().ifPresent(courtDetermination ->
            assertThat(courtDetermination).isEqualTo(ccdResponseAcceptation.getCourtDetermination()));

        actual.getClaimantPaymentIntention().ifPresent(paymentIntention ->
            assertThat(paymentIntention).isEqualTo(ccdResponseAcceptation.getClaimantPaymentIntention()));

        return this;
    }
}
