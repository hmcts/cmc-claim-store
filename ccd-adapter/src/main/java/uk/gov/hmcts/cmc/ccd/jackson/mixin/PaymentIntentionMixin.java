package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;

import java.time.LocalDate;
import java.util.Optional;

public abstract class PaymentIntentionMixin {

    abstract PaymentOption getPaymentOption();

    abstract Optional<LocalDate> getPaymentDate();

    @JsonUnwrapped
    abstract Optional<RepaymentPlan> getRepaymentPlan();
}
