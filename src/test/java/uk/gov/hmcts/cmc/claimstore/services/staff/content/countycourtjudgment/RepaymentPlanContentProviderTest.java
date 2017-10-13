package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class RepaymentPlanContentProviderTest {
    @Test
    public void createImmediatePaymentOption() {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withPaymentOption(PaymentOption.IMMEDIATELY)
            .build();

        assertThat(RepaymentPlanContentProvider.create(countyCourtJudgment)).isEqualTo("immediately");
    }

    @Test
    public void createRepaymentPlanPaymentOption() {
        LocalDate firstPaymentDate = LocalDate.now().plusDays(1);
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withPaymentOption(PaymentOption.INSTALMENTS)
            .withRepaymentPlan(SampleRepaymentPlan.builder()
                .withFirstPayment(BigDecimal.valueOf(20))
                .withInstalmentAmount(BigDecimal.valueOf(80))
                .withFirstPaymentDate(firstPaymentDate)
                .build())
            .build();

        assertThat(RepaymentPlanContentProvider.create(countyCourtJudgment))
            .isEqualTo("first payment of £20.00 on "
                + Formatting.formatDate(firstPaymentDate)
                + ". This will be followed by £80.00 each week");
    }

    @Test
    public void createBySetDatePaymentOption() {
        LocalDate now = LocalDate.now();

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withPaymentOption(PaymentOption.FULL_BY_SPECIFIED_DATE)
            .withPayBySetDate(now)
            .build();

        assertThat(RepaymentPlanContentProvider.create(countyCourtJudgment)).isEqualTo("on or before " +
            Formatting.formatDate(now));
    }
}
