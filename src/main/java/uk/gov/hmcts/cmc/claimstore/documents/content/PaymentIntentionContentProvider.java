package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.staff.content.RepaymentPlanContentProvider.create;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_PAYMENT_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Component
public class PaymentIntentionContentProvider {

    public Map<String, Object> createContent(
        PaymentOption paymentOption,
        RepaymentPlan repaymentPlan,
        LocalDate paymentDate,
        String paymentAmount,
        String source) {

        ImmutableMap.Builder<String, Object> contentBuilder = new ImmutableMap.Builder<String, Object>()
            .put(source + "paymentOption", paymentOption.getDescription())
            .put(source + "whenWillTheyFinishPaying", createWhenTheyPay(paymentOption, paymentDate,
                                                                paymentAmount, repaymentPlan));

        Optional.ofNullable(repaymentPlan).ifPresent(plan ->
            contentBuilder.put(source + "repaymentPlan", create(paymentOption, plan, plan.getFirstPaymentDate()))
        );

        return contentBuilder.build();
    }

    private String createWhenTheyPay(
        PaymentOption paymentOption,
        LocalDate paymentDate,
        String paymentAmount,
        RepaymentPlan repaymentPlan
    ) {
        switch (paymentOption) {
            case IMMEDIATELY:
            case BY_SPECIFIED_DATE:
                return paymentAmount
                    + ", no later than "
                    + formatDate(Optional.ofNullable(paymentDate)
                    .orElseThrow(() -> new IllegalStateException(MISSING_PAYMENT_DATE)));
            default:
                return repaymentPlan == null
                    ? paymentOption.getDescription()
                    : formatDate(repaymentPlan.getCompletionDate());
        }
    }
}
