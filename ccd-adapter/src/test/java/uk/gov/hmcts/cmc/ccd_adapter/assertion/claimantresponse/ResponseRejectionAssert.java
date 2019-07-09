package uk.gov.hmcts.cmc.ccd-adapter.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;

public class ResponseRejectionAssert extends AbstractAssert<ResponseRejectionAssert, ResponseRejection> {
    public ResponseRejectionAssert(ResponseRejection responseRejection) {
        super(responseRejection, ResponseRejectionAssert.class);

    }

    public ResponseRejectionAssert isEqualTo(CCDResponseRejection ccdResponseRejection) {
        isNotNull();

        actual.getFreeMediation().ifPresent(freeMediation -> {
            if (!Objects.equals(freeMediation.name(), ccdResponseRejection.getFreeMediationOption().name())) {
                failWithMessage("Expected ResponseRejection.freeMediation to be <%s> but was <%s>",
                    ccdResponseRejection.getFreeMediationOption(), freeMediation);
            }
        });

        if (ccdResponseRejection.getFreeMediationOption() != null && !actual.getFreeMediation().isPresent()) {
            failWithMessage("Expected ResponseRejection.freeMediation to be not present but was <%s>",
                actual.getFreeMediation());
        }

        actual.getMediationPhoneNumber().ifPresent(mediationPhoneNumber -> {
            if (!Objects.equals(
                mediationPhoneNumber,
                ccdResponseRejection.getMediationPhoneNumber().getTelephoneNumber())) {
                failWithMessage("Expected ResponseRejection.mediationPhoneNumber to be <%s> but was <%s>",
                    ccdResponseRejection.getMediationPhoneNumber(), mediationPhoneNumber);
            }
        });

        actual.getMediationContactPerson().ifPresent(mediationContactPerson -> {
            if (!Objects.equals(
                mediationContactPerson,
                ccdResponseRejection.getMediationContactPerson())) {
                failWithMessage("Expected ResponseRejection.mediationContactPerson to be <%s> but was <%s>",
                    ccdResponseRejection.getMediationContactPerson(), mediationContactPerson);
            }
        });

        actual.getAmountPaid().ifPresent(amountPaid -> {
            String message = String.format("Expected ResponseAcceptation.amountPaid to be <%s> but was <%s>",
                ccdResponseRejection.getAmountPaid(), amountPaid);
            assertMoney(amountPaid).isEqualTo(ccdResponseRejection.getAmountPaid(), message);
        });

        actual.getReason().ifPresent(reason -> {
            if (!Objects.equals(reason, ccdResponseRejection.getReason())) {
                failWithMessage("Expected ResponseRejection.reason to be <%s> but was <%s>",
                    ccdResponseRejection.getReason(), reason);
            }
        });

        actual.getSettleForAmount().ifPresent(settleForAmount -> {
            if (!Objects.equals(settleForAmount.name(), ccdResponseRejection.getSettleForAmount().name())) {
                failWithMessage("Expected ResponseRejection.settleForAmount to be <%s> but was <%s>",
                    ccdResponseRejection.getSettleForAmount(), settleForAmount);
            }
        });

        if (ccdResponseRejection.getSettleForAmount() != null && !actual.getSettleForAmount().isPresent()) {
            failWithMessage("Expected ResponseRejection.settleForAmount to be not present but was <%s>",
                ccdResponseRejection.getSettleForAmount());
        }

        actual.getPaymentReceived().ifPresent(paymentReceived -> {
            if (!Objects.equals(paymentReceived.name(), ccdResponseRejection.getPaymentReceived().name())) {
                failWithMessage("Expected ResponseRejection.paymentReceived to be <%s> but was <%s>",
                    ccdResponseRejection.getPaymentReceived(), paymentReceived);
            }
        });

        if (ccdResponseRejection.getPaymentReceived() != null && !actual.getPaymentReceived().isPresent()) {
            failWithMessage("Expected CCDResponseRejection.paymentReceived to be not present but was <%s>",
                ccdResponseRejection.getPaymentReceived());
        }

        return this;
    }
}
