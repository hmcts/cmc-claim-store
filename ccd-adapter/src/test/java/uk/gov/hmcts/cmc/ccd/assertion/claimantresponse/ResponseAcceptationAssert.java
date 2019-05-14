package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ResponseAcceptationAssert extends AbstractAssert<ResponseAcceptationAssert, ResponseAcceptation> {
    public ResponseAcceptationAssert(ResponseAcceptation responseAcceptation) {
        super(responseAcceptation, ResponseAcceptationAssert.class);
    }

    public ResponseAcceptationAssert isEqualTo(CCDResponseAcceptation ccdResponseAcceptation) {
        isNotNull();

        actual.getFormaliseOption().ifPresent(formaliseOption -> {
            if (!Objects.equals(formaliseOption.name(), ccdResponseAcceptation.getFormaliseOption().name())) {
                failWithMessage("Expected ResponseAcceptation.formaliseOption to be <%s> but was <%s>",
                    ccdResponseAcceptation.getFormaliseOption(), actual.getFormaliseOption());
            }
        });

        if (ccdResponseAcceptation.getFormaliseOption() != null && !actual.getFormaliseOption().isPresent()) {
            failWithMessage("Expected ResponseAcceptation.formaliseOption to be not present but was <%s>",
                actual.getFormaliseOption());
        }

        actual.getAmountPaid().ifPresent(amountPaid -> {
            String message = String.format("Expected ResponseAcceptation.amountPaid to be <%s> but was <%s>",
                ccdResponseAcceptation.getAmountPaid(), amountPaid);
            assertMoney(amountPaid).isEqualTo(ccdResponseAcceptation.getAmountPaid(), message);
        });

        actual.getCourtDetermination().ifPresent(courtDetermination -> {
            assertThat(courtDetermination).isEqualTo(ccdResponseAcceptation.getCourtDetermination());
        });

        actual.getClaimantPaymentIntention().ifPresent(paymentIntention -> {
            assertThat(paymentIntention).isEqualTo(ccdResponseAcceptation.getClaimantPaymentIntention());
        });

        actual.getSettleForAmount().ifPresent(settleForAmount -> {
            if (!Objects.equals(settleForAmount.name(), ccdResponseAcceptation.getSettleForAmount().name())) {
                failWithMessage("Expected ResponseAcceptation.settleForAmount to be <%s> but was <%s>",
                    ccdResponseAcceptation.getSettleForAmount(), settleForAmount);
            }
        });

        if (ccdResponseAcceptation.getSettleForAmount() != null && !actual.getSettleForAmount().isPresent()) {
            failWithMessage("Expected ResponseAcceptation.settleForAmount to be not present but was <%s>",
                actual.getSettleForAmount());
        }

        actual.getPaymentReceived().ifPresent(paymentReceived -> {
            if (!Objects.equals(paymentReceived.name(), ccdResponseAcceptation.getPaymentReceived().name())) {
                failWithMessage("Expected ResponseAcceptation.paymentReceived to be <%s> but was <%s>",
                    ccdResponseAcceptation.getPaymentReceived(), paymentReceived);
            }
        });

        if (ccdResponseAcceptation.getPaymentReceived() != null && !actual.getPaymentReceived().isPresent()) {
            failWithMessage("Expected ResponseAcceptation.paymentReceived to be not present but was <%s>",
                actual.getPaymentReceived());
        }

        return this;
    }
}
