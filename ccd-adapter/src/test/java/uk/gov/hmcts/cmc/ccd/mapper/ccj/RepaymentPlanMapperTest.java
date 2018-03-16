package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.math.BigDecimal;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule.EVERY_MONTH;
import static uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule.EVERY_TWO_WEEKS;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RepaymentPlanMapperTest {

    @Autowired
    private RepaymentPlanMapper repaymentPlanMapper;

    @Test
    public void shouldMapRepaymentPlanToCCD() {
        //given
        final RepaymentPlan repaymentPlan = SampleRepaymentPlan.builder().build();

        //when
        CCDRepaymentPlan ccdRepaymentPlan = repaymentPlanMapper.to(repaymentPlan);

        //then
        assertThat(repaymentPlan).isEqualTo(ccdRepaymentPlan);
    }

    @Test
    public void shouldMapMonthlyRepaymentPlanToCCD() {
        //given
        final RepaymentPlan repaymentPlan = SampleRepaymentPlan.builder().withPaymentSchedule(EVERY_MONTH).build();

        //when
        CCDRepaymentPlan ccdRepaymentPlan = repaymentPlanMapper.to(repaymentPlan);

        //then
        assertThat(repaymentPlan).isEqualTo(ccdRepaymentPlan);
    }

    @Test
    public void shouldMapFortnightlyRepaymentPlanToCCD() {
        //given
        final RepaymentPlan repaymentPlan = SampleRepaymentPlan.builder().withPaymentSchedule(EVERY_TWO_WEEKS).build();

        //when
        CCDRepaymentPlan ccdRepaymentPlan = repaymentPlanMapper.to(repaymentPlan);

        //then
        assertThat(repaymentPlan).isEqualTo(ccdRepaymentPlan);
    }

    @Test
    public void shouldMapRepaymentPlanFromCCD() {
        //given
        final CCDRepaymentPlan ccdRepaymentPlan = CCDRepaymentPlan.builder()
            .paymentSchedule(CCDPaymentSchedule.EACH_WEEK)
            .instalmentAmount(BigDecimal.valueOf(100))
            .firstPaymentDate(now())
            .build();

        //when
        RepaymentPlan repaymentPlan = repaymentPlanMapper.from(ccdRepaymentPlan);

        //then
        assertThat(repaymentPlan).isEqualTo(ccdRepaymentPlan);
    }

    @Test
    public void shouldMapMonthlyRepaymentPlanFromCCD() {
        //given
        final CCDRepaymentPlan ccdRepaymentPlan = CCDRepaymentPlan.builder()
            .paymentSchedule(CCDPaymentSchedule.EVERY_MONTH)
            .instalmentAmount(BigDecimal.valueOf(100))
            .firstPaymentDate(now())
            .build();

        //when
        RepaymentPlan repaymentPlan = repaymentPlanMapper.from(ccdRepaymentPlan);

        //then
        assertThat(repaymentPlan).isEqualTo(ccdRepaymentPlan);
    }

    @Test
    public void shouldMapFortnightlyRepaymentPlanFromCCD() {
        //given
        final CCDRepaymentPlan ccdRepaymentPlan = CCDRepaymentPlan.builder()
            .paymentSchedule(CCDPaymentSchedule.EVERY_TWO_WEEKS)
            .instalmentAmount(BigDecimal.valueOf(100))
            .firstPaymentDate(now())
            .build();

        //when
        RepaymentPlan repaymentPlan = repaymentPlanMapper.from(ccdRepaymentPlan);

        //then
        assertThat(repaymentPlan).isEqualTo(ccdRepaymentPlan);
    }
}
