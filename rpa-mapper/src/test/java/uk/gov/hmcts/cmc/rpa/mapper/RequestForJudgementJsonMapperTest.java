package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment.CountyCourtJudgmentBuilder;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings({"LineLength"})
public class RequestForJudgementJsonMapperTest {

    private static final LocalDateTime CCJ_REQUESTED_AT = LocalDate.of(2018, 4, 26).atStartOfDay();
    private static final LocalDate PAY_BY_SET_DATE = LocalDate.of(2200, 3, 12);
    private static final BigDecimal PAID_ALREADY = new BigDecimal(10);
    private static final LocalDate FIRST_PAYMENT_DATE = LocalDate.of(2200, 3, 2);

    @Autowired
    private RequestForJudgementJsonMapper mapper;

    @Test
    public void shouldMapRequestForJudgementImmediatelyWithPaidAlready() throws JSONException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paidAmount(PAID_ALREADY)
            .build();

        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(CCJ_REQUESTED_AT)
            .withCountyCourtJudgment(countyCourtJudgment)
            .build();

        String expected = new ResourceReader()
            .read("/judgement/rpa_request_for_judgement_immediately.json").trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementImmediatelyButNothingPaid() throws JSONException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paidAmount(null)
            .build();

        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(CCJ_REQUESTED_AT)
            .withCountyCourtJudgment(countyCourtJudgment)
            .build();

        String expected = new ResourceReader()
            .read("/judgement/rpa_request_for_judgement_immediately_nothing_paid.json").trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementPaidInFull() throws JSONException {
        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment
            .builder()
            .paidAmount(PAID_ALREADY)
            .paymentOption(PaymentOption.BY_SPECIFIED_DATE)
            .payBySetDate(PAY_BY_SET_DATE)
            .build();

        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(CCJ_REQUESTED_AT)
            .withCountyCourtJudgment(countyCourtJudgment)
            .build();

        String expected = new ResourceReader()
            .read("/judgement/rpa_request_for_judgement_by_set_date.json").trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementPayingFortnightly() throws JSONException {
        RepaymentPlan repaymentPlan = SampleRepaymentPlan
            .builder()
            .paymentSchedule(PaymentSchedule.EVERY_TWO_WEEKS)
            .firstPaymentDate(FIRST_PAYMENT_DATE)
            .instalmentAmount(BigDecimal.valueOf(100.00))
            .build();

        CountyCourtJudgmentBuilder countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .paidAmount(PAID_ALREADY)
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(repaymentPlan);

        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(CCJ_REQUESTED_AT)
            .withCountyCourtJudgment(countyCourtJudgment.build())
            .build();

        String expected = new ResourceReader()
            .read("/judgement/rpa_request_for_judgement_fortnightly.json").trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementPayingWeekly() throws JSONException {
        RepaymentPlan repaymentPlan = SampleRepaymentPlan
            .builder()
            .paymentSchedule(PaymentSchedule.EACH_WEEK)
            .firstPaymentDate(PAY_BY_SET_DATE)
            .build();

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .paidAmount(PAID_ALREADY)
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(repaymentPlan)
            .build();

        Claim claim = SampleClaim.builder().withDefendantEmail("defendant@email.com")
            .withCountyCourtJudgmentRequestedAt(CCJ_REQUESTED_AT)
            .withCountyCourtJudgment(countyCourtJudgment)
            .build();

        String expected = new ResourceReader().read("/judgement/rpa_request_for_judgement_weekly.json")
            .trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementPayingMonthly() throws JSONException {
        RepaymentPlan repaymentPlan = SampleRepaymentPlan.builder()
            .paymentSchedule(PaymentSchedule.EVERY_MONTH)
            .firstPaymentDate(PAY_BY_SET_DATE)
            .build();

        CountyCourtJudgment countyCourtJudgment = SampleCountyCourtJudgment.builder()
            .paidAmount(PAID_ALREADY)
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(repaymentPlan)
            .build();

        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(CCJ_REQUESTED_AT)
            .withCountyCourtJudgment(countyCourtJudgment)
            .build();

        String expected = new ResourceReader()
            .read("/judgement/rpa_request_for_judgement_monthly.json").trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }
}
