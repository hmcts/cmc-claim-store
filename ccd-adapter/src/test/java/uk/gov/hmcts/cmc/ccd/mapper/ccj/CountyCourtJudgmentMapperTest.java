package uk.gov.hmcts.cmc.ccd.mapper.ccj;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentOption;
import uk.gov.hmcts.cmc.ccd.domain.CCDPaymentSchedule;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Function;

import static java.time.LocalDate.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CountyCourtJudgmentMapperTest {

    @Autowired
    private CountyCourtJudgmentMapper countyCourtJudgmentMapper;

    private Function<CountyCourtJudgment, Claim> createClaimWithCCJ = ccj -> Claim.builder().countyCourtJudgment(ccj)
        .countyCourtJudgmentRequestedAt(LocalDateTime.now())
        .build();

    @Test
    public void mapEmptyCCJWillReturnNull() {
        assertNull(countyCourtJudgmentMapper.to(null));
        assertNull(countyCourtJudgmentMapper.to(Claim.builder().build()));
    }

    @Test
    public void mapEmptyCCDCountyCourtJudgmentDontError() {
        //Given
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //When
        countyCourtJudgmentMapper.from(null, claimBuilder);
        Claim finalClaim = claimBuilder.build();

        //Then
        assertNull(finalClaim.getCountyCourtJudgment());
    }

    @Test
    public void shouldMapCountyCourtJudgmentToCCD() {
        //given
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .build();
        Claim claim = createClaimWithCCJ.apply(countyCourtJudgment);

        //when
        CCDCountyCourtJudgment ccdCountyCourtJudgment = countyCourtJudgmentMapper.to(claim);

        //then
        assertNotNull(ccdCountyCourtJudgment);
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentWithPaymentOptionImmediatelyToCCD() {
        //given
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .paymentOption(PaymentOption.IMMEDIATELY)
            .paidAmount(BigDecimal.TEN)
            .statementOfTruth(new StatementOfTruth("Tester", "Unit Test"))
            .build();
        Claim claim = createClaimWithCCJ.apply(countyCourtJudgment);

        //when
        CCDCountyCourtJudgment ccdCountyCourtJudgment = countyCourtJudgmentMapper.to(claim);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentWithPaymentOptionSpecifiedDateToCCD() {
        //given
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .payBySetDate(now())
            .statementOfTruth(new StatementOfTruth("Tester", "Unit Test"))
            .build();
        Claim claim = createClaimWithCCJ.apply(countyCourtJudgment);

        //when
        CCDCountyCourtJudgment ccdCountyCourtJudgment = countyCourtJudgmentMapper.to(claim);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
    }

    @Test
    public void shouldMapCountyCourtJudgmentWithPaymentOptionInstalmentsToCCD() {
        //given
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(SampleRepaymentPlan.builder().build())
            .statementOfTruth(new StatementOfTruth("Tester", "Unit Test"))
            .build();
        Claim claim = createClaimWithCCJ.apply(countyCourtJudgment);

        //when
        CCDCountyCourtJudgment ccdCountyCourtJudgment = countyCourtJudgmentMapper.to(claim);

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
        assertEquals(claim.getCountyCourtJudgmentRequestedAt(), ccdCountyCourtJudgment.getRequestedDate());
    }

    @Test
    public void shouldMapCountyCourtJudgmentFromCCD() {
        //given
        CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .paymentOption(CCDPaymentOption.IMMEDIATELY)
            .paidAmount("1000")
            .statementOfTruthSignerRole("PM")
            .statementOfTruthSignerName("Mrs May")
            .type(CCDCountyCourtJudgmentType.ADMISSIONS)
            .build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        countyCourtJudgmentMapper.from(ccdCountyCourtJudgment, claimBuilder);
        Claim claim = claimBuilder.build();
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
        assertEquals(claim.getCountyCourtJudgmentRequestedAt(), ccdCountyCourtJudgment.getRequestedDate());
    }

    @Test
    public void shouldMapCountyCourtJudgmentWithRepaymentPlanFromCCD() {
        //given

        CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .paymentOption(CCDPaymentOption.INSTALMENTS)
            .repaymentPlanCompletionDate(now().plusMonths(5))
            .repaymentPlanFirstPaymentDate(now())
            .repaymentPlanPaymentLength("1 light years")
            .repaymentPlanInstalmentAmount("1000")
            .repaymentPlanPaymentSchedule(CCDPaymentSchedule.EACH_WEEK)
            .paidAmount("1000")
            .statementOfTruthSignerName("Mrs May")
            .statementOfTruthSignerRole("PM")
            .type(CCDCountyCourtJudgmentType.ADMISSIONS)
            .build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        countyCourtJudgmentMapper.from(ccdCountyCourtJudgment, claimBuilder);
        Claim claim = claimBuilder.build();
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
        assertEquals(claim.getCountyCourtJudgmentRequestedAt(), ccdCountyCourtJudgment.getRequestedDate());
    }

    @Test
    public void shouldMapCountyCourtJudgmentFullBySetDateFromCCD() {
        //given
        CCDCountyCourtJudgment ccdCountyCourtJudgment = CCDCountyCourtJudgment.builder()
            .defendantDateOfBirth(now().minusYears(20))
            .paymentOption(CCDPaymentOption.BY_SPECIFIED_DATE)
            .payBySetDate(now())
            .paidAmount("1000")
            .statementOfTruthSignerRole("PM")
            .statementOfTruthSignerName("Mrs May")
            .type(CCDCountyCourtJudgmentType.DETERMINATION)
            .build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        //when
        countyCourtJudgmentMapper.from(ccdCountyCourtJudgment, claimBuilder);
        Claim claim = claimBuilder.build();
        CountyCourtJudgment countyCourtJudgment = claim.getCountyCourtJudgment();

        //then
        assertThat(countyCourtJudgment).isEqualTo(ccdCountyCourtJudgment);
        assertEquals(claim.getCountyCourtJudgmentRequestedAt(), ccdCountyCourtJudgment.getRequestedDate());
    }
}
