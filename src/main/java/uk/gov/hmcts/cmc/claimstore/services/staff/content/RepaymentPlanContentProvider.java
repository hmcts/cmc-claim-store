package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import uk.gov.hmcts.cmc.claimstore.services.staff.models.RepaymentPlanContent;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;

public final class RepaymentPlanContentProvider {

    private RepaymentPlanContentProvider() {
        // Utility class
    }

    public static RepaymentPlanContent create(
        PaymentOption paymentOption,
        RepaymentPlan repaymentPlan,
        LocalDate payBySetDate
    ) {
        switch (paymentOption) {
            case IMMEDIATELY:
                return new RepaymentPlanContent(IMMEDIATELY.getDescription());
            case INSTALMENTS:
                requireNonNull(repaymentPlan, "repaymentPlan must not be null");
                return new RepaymentPlanContent(
                    INSTALMENTS.getDescription(),
                    formatMoney(repaymentPlan.getInstalmentAmount()),
                    formatDate(repaymentPlan.getFirstPaymentDate()),
                    repaymentPlan.getPaymentSchedule().getDescription()
                );
            case BY_SPECIFIED_DATE:
                requireNonNull(payBySetDate, "payBySetDate must not be null");
                return new RepaymentPlanContent(BY_SPECIFIED_DATE.getDescription(), formatDate(payBySetDate));
            default:
                throw new IllegalArgumentException("Unknown repayment type: " + paymentOption);
        }
    }
}
