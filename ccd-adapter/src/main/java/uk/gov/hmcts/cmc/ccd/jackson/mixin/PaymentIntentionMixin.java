package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;

import java.util.Optional;

public abstract class PaymentIntentionMixin {

    @JsonUnwrapped
    public abstract Optional<RepaymentPlan> getRepaymentPlan();
}
