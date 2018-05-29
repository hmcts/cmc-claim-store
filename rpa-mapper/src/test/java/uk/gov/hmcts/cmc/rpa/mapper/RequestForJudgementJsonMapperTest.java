package uk.gov.hmcts.cmc.rpa.mapper;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ccj.PaymentSchedule;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.cmc.rpa.config.ModuleConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

@SpringBootTest
@ContextConfiguration(classes = ModuleConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RequestForJudgementJsonMapperTest {

    @Autowired
    private RequestForJudgementJsonMapper mapper;
    private static final LocalDate ISSUED_DATE = LocalDate.of(2018, 4, 26);

    @Test
    public void shouldMapRequestForJudgementForthWith() throws JSONException {
        SampleCountyCourtJudgment countyCourtJudgment = new SampleCountyCourtJudgment();
        countyCourtJudgment.withPaidAmount(new BigDecimal(10.00));
        Claim claim = SampleClaim.builder()
            .withIssuedOn(ISSUED_DATE)
            .withCountyCourtJudgment(countyCourtJudgment.build()).build();
        String expected = new ResourceReader().read("/judgement/rpa_request_for_judgement_forthwith.json")
            .trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementNothingPaid() throws JSONException {
        SampleCountyCourtJudgment countyCourtJudgment = new SampleCountyCourtJudgment();
        countyCourtJudgment.withPaidAmount(null);
        Claim claim = SampleClaim.builder()
            .withIssuedOn(ISSUED_DATE)
            .withCountyCourtJudgment(countyCourtJudgment.build()).build();
        String expected = new ResourceReader().read("/judgement/rpa_request_for_judgement_nothing_paid.json")
            .trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementPaidInFull() throws JSONException {
        SampleCountyCourtJudgment countyCourtJudgment = new SampleCountyCourtJudgment();
        countyCourtJudgment.withPaidAmount(new BigDecimal(10.00));
        countyCourtJudgment.withPayBySetDate(LocalDate.of(2200, 3, 12));
        Claim claim = SampleClaim.builder()
            .withIssuedOn(ISSUED_DATE)
            .withCountyCourtJudgment(countyCourtJudgment.build()).build();
        String expected = new ResourceReader().read("/judgement/rpa_request_for_judgement_full.json")
            .trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementPayingFortnightly() throws JSONException {
        SampleCountyCourtJudgment countyCourtJudgment = new SampleCountyCourtJudgment();
        countyCourtJudgment.withPaidAmount(new BigDecimal(10.00));
        SampleRepaymentPlan sampleRepaymentPlan = SampleRepaymentPlan
            .builder()
            .withPaymentSchedule(PaymentSchedule.EVERY_TWO_WEEKS)
            .withFirstPaymentDate(LocalDate.of(2200, 3, 12));
        countyCourtJudgment.withRepaymentPlan(sampleRepaymentPlan.build());

        Claim claim = SampleClaim.builder()
            .withIssuedOn(ISSUED_DATE)
            .withCountyCourtJudgment(countyCourtJudgment.build()).build();

        String expected = new ResourceReader().read("/judgement/rpa_request_for_judgement_fortnightly.json")
            .trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementPayingWeekly() throws JSONException {
        SampleCountyCourtJudgment countyCourtJudgment = new SampleCountyCourtJudgment();
        countyCourtJudgment.withPaidAmount(new BigDecimal(10.00));
        SampleRepaymentPlan sampleRepaymentPlan = SampleRepaymentPlan
            .builder()
            .withPaymentSchedule(PaymentSchedule.EACH_WEEK)
            .withFirstPaymentDate(LocalDate.of(2200, 3, 12));
        countyCourtJudgment.withRepaymentPlan(sampleRepaymentPlan.build());

        Claim claim = SampleClaim.builder()
            .withIssuedOn(ISSUED_DATE)
            .withCountyCourtJudgment(countyCourtJudgment.build()).build();

        String expected = new ResourceReader().read("/judgement/rpa_request_for_judgement_weekly.json")
            .trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }

    @Test
    public void shouldMapRequestForJudgementPayingMonthly() throws JSONException {
        SampleCountyCourtJudgment countyCourtJudgment = new SampleCountyCourtJudgment();
        countyCourtJudgment.withPaidAmount(new BigDecimal(10.00));
        SampleRepaymentPlan sampleRepaymentPlan = SampleRepaymentPlan
            .builder()
            .withPaymentSchedule(PaymentSchedule.EVERY_MONTH)
            .withFirstPaymentDate(LocalDate.of(2200, 3, 12));
        countyCourtJudgment.withRepaymentPlan(sampleRepaymentPlan.build());

        Claim claim = SampleClaim.builder()
            .withIssuedOn(ISSUED_DATE)
            .withCountyCourtJudgment(countyCourtJudgment.build()).build();

        String expected = new ResourceReader().read("/judgement/rpa_request_for_judgement_monthly.json")
            .trim();
        assertEquals(expected, mapper.map(claim).toString(), STRICT);
    }
}
