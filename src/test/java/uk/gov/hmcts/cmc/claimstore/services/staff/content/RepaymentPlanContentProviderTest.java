package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.RepaymentPlanContent;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.math.BigDecimal;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.services.staff.content.RepaymentPlanContentProvider.create;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.PaymentOption.FULL_BY_SPECIFIED_DATE;
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
        RepaymentPlan repaymentPlan = SampleRepaymentPlan.builder()
            .withInstalmentAmount(BigDecimal.valueOf(80))
            .withFirstPaymentDate(now().plusDays(1))
            .build();

        RepaymentPlanContent repaymentPlanContent = create(INSTALMENTS, repaymentPlan, null);

        assertThat(repaymentPlanContent.getRepaymentOption())
            .isEqualTo("By instalments");

        assertThat(repaymentPlanContent.getInstalmentAmount())
            .isEqualTo(formatMoney(BigDecimal.valueOf(80)));
    }

    @Test
    public void createBySetDatePaymentOption() {
        RepaymentPlanContent repaymentPlanContent = create(FULL_BY_SPECIFIED_DATE, null, now());

        assertThat(repaymentPlanContent.getRepaymentOption())
            .isEqualTo("By a set date");
        assertThat(repaymentPlanContent.getPaySetByDate())
            .isEqualTo(formatDate(now()));
    }
}
