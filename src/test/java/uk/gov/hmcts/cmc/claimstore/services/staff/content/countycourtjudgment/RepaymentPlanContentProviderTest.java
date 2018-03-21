package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

public class RepaymentPlanContentProviderTest {
    @Test
    public void createImmediatePaymentOption() {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withPaymentOption(PaymentOption.IMMEDIATELY)
            .build();


        assertThat(RepaymentPlanContentProvider.create(countyCourtJudgment).getRepaymentOption())
            .isEqualTo("Immediately");
    }

    @Test
    public void createRepaymentPlanPaymentOption() {
        LocalDate firstPaymentDate = LocalDate.now().plusDays(1);
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withPaymentOption(PaymentOption.INSTALMENTS)
            .withRepaymentPlan(SampleRepaymentPlan.builder()
                .withInstalmentAmount(BigDecimal.valueOf(80))
                .withFirstPaymentDate(firstPaymentDate)
                .build())
            .build();

        assertThat(RepaymentPlanContentProvider.create(countyCourtJudgment).getRepaymentOption())
            .isEqualTo("By instalments");
        assertThat(RepaymentPlanContentProvider.create(countyCourtJudgment).getInstalmentAmount())
            .isEqualTo(formatMoney(BigDecimal.valueOf(80)));
    }

    @Test
    public void createBySetDatePaymentOption() {
        LocalDate now = LocalDate.now();

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withPaymentOption(PaymentOption.FULL_BY_SPECIFIED_DATE)
            .withPayBySetDate(now)
            .build();

        assertThat(RepaymentPlanContentProvider.create(countyCourtJudgment).getPaySetByDate())
            .isEqualTo(formatDate(now));
    }
}
