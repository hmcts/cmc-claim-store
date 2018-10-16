package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantInvalidRepaymentPlanException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimantRepaymentPlanRule;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.DEFAULT;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentServiceTest {

    private static final CountyCourtJudgment DATA = SampleCountyCourtJudgment.builder().build();
    private static final String AUTHORISATION = "Bearer: aaa";

    private CountyCourtJudgmentService countyCourtJudgmentService;

    private ClaimantRepaymentPlanRule claimantRepaymentPlanRule = new ClaimantRepaymentPlanRule();

    @Mock
    private ClaimService claimService;

    @Mock
    private EventProducer eventProducer;
    @Mock
    private AppInsights appInsights;

    @Before
    public void setup() {

        countyCourtJudgmentService = new CountyCourtJudgmentService(
            claimService,
            new AuthorisationService(),
            eventProducer,
            new CountyCourtJudgmentRule(new ClaimDeadlineService()),
            claimantRepaymentPlanRule,
            appInsights);
    }

    @Test
    public void saveShouldFinishCCJRequestSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2)).build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(USER_ID, ccj, EXTERNAL_ID, AUTHORISATION, false);

        verify(eventProducer, once()).createCountyCourtJudgmentEvent(any(Claim.class), any(), eq(false));
        verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any(), eq(false));
        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_REQUESTED), eq(claim.getReferenceNumber()));
    }

    @Test
    public void saveShouldFinishCCJIssueSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        countyCourtJudgmentService.save(USER_ID, DATA, EXTERNAL_ID, AUTHORISATION, true);

        verify(eventProducer, once()).createCountyCourtJudgmentEvent(any(Claim.class), any(), eq(true));
        verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any(), eq(true));
        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_ISSUED), eq(claim.getReferenceNumber()));
    }

    @Test(expected = NotFoundException.class)
    public void saveThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenThrow(new NotFoundException("Claim not found by id"));

        countyCourtJudgmentService.save(USER_ID, DATA, EXTERNAL_ID, AUTHORISATION, false);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        String differentUser = "34234234";

        Claim claim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        countyCourtJudgmentService.save(differentUser, DATA, EXTERNAL_ID, AUTHORISATION, false);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasResponded() {
        Claim respondedClaim = SampleClaim.builder().withRespondedAt(LocalDateTime.now().minusDays(2)).build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        countyCourtJudgmentService.save(USER_ID, DATA, EXTERNAL_ID, AUTHORISATION, false);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenUserCannotRequestCountyCourtJudgmentYet() {
        Claim respondedClaim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        countyCourtJudgmentService.save(USER_ID, DATA, EXTERNAL_ID, AUTHORISATION, false);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {
        Claim respondedClaim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        countyCourtJudgmentService.save(USER_ID, DATA, EXTERNAL_ID, AUTHORISATION, false);
    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void saveThrowsExceptionWhenClaimantRepaymentPlanStartDateDoesNotMeetCriteria() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .withRepaymentPlan(
                SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now()).build())
            .build();

        Claim invalidClaim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder()
                .paymentIntention(SamplePaymentIntention.builder()
                    .paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(
                        SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now().plusMonths(2)).build())
                    .build())
                .build()
        );

        when(claimService.getClaimByExternalId(eq(invalidClaim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(invalidClaim);

        countyCourtJudgmentService.save(USER_ID, ccj, invalidClaim.getExternalId(), AUTHORISATION, true);

    }
}
