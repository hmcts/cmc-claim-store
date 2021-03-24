package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentRuleTest {

    @Captor
    private ArgumentCaptor<LocalDateTime> currentDateTime;

    @Spy
    private ClaimDeadlineService claimDeadlineService = new ClaimDeadlineService();

    private CountyCourtJudgmentRule countyCourtJudgmentRule;

    @Before
    public void beforeEachTest() {
        countyCourtJudgmentRule = new CountyCourtJudgmentRule(claimDeadlineService);
    }

    @Test
    public void shouldNotThrowExceptionWhenDeadlineWasYesterdayAndCCJCanBeRequested() {
        Claim claim = SampleClaim.builder().withResponseDeadline(LocalDate.now().minusDays(1)).build();
        assertThatCode(() ->
            countyCourtJudgmentRule.assertCountyCourtJudgmentCanBeRequested(claim, CountyCourtJudgmentType.DEFAULT)
        ).doesNotThrowAnyException();
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenUserCannotRequestCountyCourtJudgmentBecauseDeadlineIsTomorrow() {
        Claim claim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(1));
        countyCourtJudgmentRule.assertCountyCourtJudgmentCanBeRequested(claim, CountyCourtJudgmentType.DEFAULT);
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenClaimWasResponded() {
        Claim respondedClaim = SampleClaim.builder().withRespondedAt(now().minusDays(2)).build();
        countyCourtJudgmentRule.assertCountyCourtJudgmentCanBeRequested(respondedClaim,
            CountyCourtJudgmentType.DEFAULT);
    }

    @Test
    public void shouldCallClaimDeadlineServicePassingCurrentUKTimeToCheckIfJudgementCanBeRequested() {
        LocalDate deadlineDay = LocalDate.now().minusMonths(2);
        Claim claim = SampleClaim.builder().withResponseDeadline(deadlineDay).build();
        countyCourtJudgmentRule.assertCountyCourtJudgmentCanBeRequested(claim, CountyCourtJudgmentType.DEFAULT);

        verify(claimDeadlineService).isPastDeadline(currentDateTime.capture(), eq(deadlineDay));
        assertThat(currentDateTime.getValue()).isCloseTo(nowInLocalZone(), within(10, ChronoUnit.SECONDS));
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {
        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(now())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(PaymentOption.IMMEDIATELY)
                    .build()
            ).build();
        countyCourtJudgmentRule.assertCountyCourtJudgmentCanBeRequested(claim, CountyCourtJudgmentType.DEFAULT);
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldThrowExceptionWhenCountyCourtJudgmentWasForAdmissionResponse() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.validDefaults())
            .withCountyCourtJudgmentRequestedAt(now())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(PaymentOption.IMMEDIATELY)
                    .build()
            ).build();
        countyCourtJudgmentRule.assertCountyCourtJudgmentCanBeRequested(claim, CountyCourtJudgmentType.ADMISSIONS);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenCountyCourtJudgmentWasForAdmissionResponseThatHasNoResponse() {
        Claim claim = SampleClaim.builder()
            .withCountyCourtJudgmentRequestedAt(now())
            .build();
        countyCourtJudgmentRule.assertCountyCourtJudgmentCanBeRequested(claim, CountyCourtJudgmentType.ADMISSIONS);
    }

    @Test
    public void shouldReturnFalseWhenIsCCJDueToSettlementBreachAndClaimHasNoSettlement() {
        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.validDefaults())
            .withCountyCourtJudgmentRequestedAt(now())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(PaymentOption.IMMEDIATELY)
                    .build()
            ).build();
        assertThat(countyCourtJudgmentRule.isCCJDueToSettlementBreach(claim)).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenIsCCJDueToSettlementBreachAndClaimSettlementDateIsInTheFuture() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builderWithPaymentIntention().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.validDefaults())
            .withSettlement(settlement)
            .withCountyCourtJudgmentRequestedAt(now())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(PaymentOption.IMMEDIATELY)
                    .build()
            ).build();
        assertThat(countyCourtJudgmentRule.isCCJDueToSettlementBreach(claim)).isFalse();
    }

    @Test
    public void shouldReturnTrueWhenIsCCJDueToSettlementBreachAndClaimSettlementDateIsInThePast() {
        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builderWithSetByDateInPast().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.validDefaults())
            .withSettlement(settlement)
            .withCountyCourtJudgmentRequestedAt(now())
            .withCountyCourtJudgment(
                SampleCountyCourtJudgment.builder()
                    .paymentOption(PaymentOption.IMMEDIATELY)
                    .build()
            ).build();
        assertThat(countyCourtJudgmentRule.isCCJDueToSettlementBreach(claim)).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenCountyCourtJudgmentCannotBeRequested() {
        Claim claim = SampleClaim.builder()
            .withClaimId(12345678L)
            .withClaimData(ClaimData.builder().build())
            .build();

        assertThatCode(() ->
            countyCourtJudgmentRule.assertCountyCourtJudgmentCannotBeRequested(claim)
        ).doesNotThrowAnyException();
    }

    @Test(expected = ForbiddenActionException.class)
    public void shouldReturnTureWhenCountyCourtJudgmentCannotBeRequested() {
        BreathingSpace breathingSpace = BreathingSpace.builder()
            .bsEnteredDate(LocalDate.now())
            .bsLiftedDate(null)
            .build();
        Claim claim = SampleClaim.builder()
            .withClaimId(12345678L)
            .withClaimData(ClaimData.builder()
                .breathingSpace(breathingSpace)
                .build())
            .build();
        countyCourtJudgmentRule.assertCountyCourtJudgmentCannotBeRequested(claim);
    }
}
