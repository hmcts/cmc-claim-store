package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.RepaymentPlanContent;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.content.RepaymentPlanContentProvider.create;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.BY_SPECIFIED_DATE;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.IMMEDIATELY;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.INSTALMENTS;

public class RepaymentPlanContentProviderTest {

    @Test
    public void createImmediatePaymentOption() {
        assertThat(create(IMMEDIATELY, null, null).getRepaymentOption())
            .isEqualTo("Immediately");
    }

    @Test
    public void createRepaymentPlanPaymentOption() {
        LocalDate firstPaymentDate = now().plusDays(1);
        RepaymentPlan repaymentPlan = SampleRepaymentPlan.builder()
            .instalmentAmount(BigDecimal.valueOf(80))
            .paymentSchedule(PaymentSchedule.EVERY_MONTH)
            .firstPaymentDate(firstPaymentDate)
            .build();

        RepaymentPlanContent repaymentPlanContent = create(INSTALMENTS, repaymentPlan, null);

        assertThat(repaymentPlanContent.getRepaymentOption())
            .isEqualTo("By instalments");

        assertThat(repaymentPlanContent.getInstalmentAmount())
            .isEqualTo(formatMoney(BigDecimal.valueOf(80)));

        assertThat(repaymentPlanContent.getFirstPaymentDate())
            .isEqualTo(formatDate(firstPaymentDate));
    }

    @Test
    public void createBySetDatePaymentOption() {
        LocalDate setDate = now();
        RepaymentPlanContent repaymentPlanContent = create(BY_SPECIFIED_DATE, null, setDate);

        assertThat(repaymentPlanContent.getRepaymentOption())
            .isEqualTo("By a set date");
        assertThat(repaymentPlanContent.getPaySetByDate())
            .isEqualTo(formatDate(setDate));
    }
}
