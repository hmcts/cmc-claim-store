package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.math.BigDecimal;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CountyCourtJudgmentMapperTest {

    @Autowired
    private CountyCourtJudgmentMapper countyCourtJudgmentMapper;

    @Test
    public void shouldMapCountyCourtJudgmentToCCD() {
        //given
        final CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder().build();

        //when
        CCDCountyCourtJudgment ccdCountyCourtJudgment = countyCourtJudgmentMapper.to(countyCourtJudgment);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentWithPaymentOptionImmediatelyToCCD() {
        //given
        final CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withPaymentOptionImmediately()
            .withPaidAmount(BigDecimal.TEN)
            .withStatementOfTruth(new StatementOfTruth("Tester", "Unit Test"))
            .build();

        //when
        CCDCountyCourtJudgment ccdCountyCourtJudgment = countyCourtJudgmentMapper.to(countyCourtJudgment);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentWithPaymentOptionSpecifiedDateToCCD() {
        //given
        final CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withPayBySetDate(now())
            .withStatementOfTruth(new StatementOfTruth("Tester", "Unit Test"))
            .build();

        //when
        CCDCountyCourtJudgment ccdCountyCourtJudgment = countyCourtJudgmentMapper.to(countyCourtJudgment);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentWithPaymentOptionInstalmentsToCCD() {
        //given
        final CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .withRepaymentPlan(SampleRepaymentPlan.builder().build())
            .withStatementOfTruth(new StatementOfTruth("Tester", "Unit Test"))
            .build();

        //when
        CCDCountyCourtJudgment ccdCountyCourtJudgment = countyCourtJudgmentMapper.to(countyCourtJudgment);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentFromCCD() {
        //given
        final CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .paymentOption(CCDPaymentOption.IMMEDIATELY)
            .paidAmount(BigDecimal.TEN)
            .statementOfTruth(CCDStatementOfTruth.builder().signerName("Tester").signerRole("Unit Test").build())
            .build();

        //when
        CountyCourtJudgment countyCourtJudgment = countyCourtJudgmentMapper.from(ccdCountyCourtJudgment);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentWithRepaymentPlanFromCCD() {
        //given

        final CCDRepaymentPlan ccdRepaymentPlan = CCDRepaymentPlan.builder()
            .paymentSchedule(CCDPaymentSchedule.EVERY_TWO_WEEKS)
            .instalmentAmount(BigDecimal.valueOf(100))
            .firstPaymentDate(now())
            .build();

        final CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .paymentOption(CCDPaymentOption.INSTALMENTS)
            .repaymentPlan(ccdRepaymentPlan)
            .paidAmount(BigDecimal.TEN)
            .statementOfTruth(CCDStatementOfTruth.builder().signerName("Tester").signerRole("Unit Test").build())
            .build();

        //when
        CountyCourtJudgment countyCourtJudgment = countyCourtJudgmentMapper.from(ccdCountyCourtJudgment);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentFullBySetDateFromCCD() {
        //given
        final CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .defendantDateOfBirth(now().minusYears(20))
            .paymentOption(CCDPaymentOption.FULL_BY_SPECIFIED_DATE)
            .payBySetDate(now())
            .paidAmount(BigDecimal.TEN)
            .statementOfTruth(CCDStatementOfTruth.builder().signerName("Tester").signerRole("Unit Test").build())
            .build();

        //when
        CountyCourtJudgment countyCourtJudgment = countyCourtJudgmentMapper.from(ccdCountyCourtJudgment);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }
}
