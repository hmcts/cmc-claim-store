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
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
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

    @Mock
    private ClaimService claimService;
    @Mock
    private UserService userService;

    @Mock
    private EventProducer eventProducer;
    @Mock
    private AppInsights appInsights;

    private ReDetermination reDetermination = ReDetermination.builder()
        .explanation("I feel defendant can pay")
        .partyType(MadeBy.CLAIMANT)
        .build();

    private UserDetails userDetails = SampleUserDetails.builder().withUserId(USER_ID).build();

    @Before
    public void setup() {

        countyCourtJudgmentService = new CountyCourtJudgmentService(
            claimService,
            new AuthorisationService(),
            eventProducer,
            new CountyCourtJudgmentRule(new ClaimDeadlineService()),
            userService,
            appInsights);

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
    }

    @Test
    public void saveShouldFinishCCJRequestSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2)).build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(ccj, EXTERNAL_ID, AUTHORISATION, false);

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

        countyCourtJudgmentService.save(DATA, EXTERNAL_ID, AUTHORISATION, true);

        verify(eventProducer, once()).createCountyCourtJudgmentEvent(any(Claim.class), any(), eq(true));
        verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any(), eq(true));
        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_ISSUED), eq(claim.getReferenceNumber()));
    }

    @Test
    public void reDeterminationShouldFinishSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
            .withCountyCourtJudgmentRequestedAt(LocalDate.of(2018, 4, 26).atStartOfDay())
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);

        verify(eventProducer, once()).createRedeterminationEvent(any(Claim.class),
            eq(AUTHORISATION), eq(userDetails.getFullName()));

        verify(claimService, once()).saveReDetermination(eq(AUTHORISATION), any(), eq(reDetermination), eq(USER_ID));
    }

    @Test(expected = NotFoundException.class)
    public void saveThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenThrow(new NotFoundException("Claim not found by id"));

        countyCourtJudgmentService.save(DATA, EXTERNAL_ID, AUTHORISATION, false);
    }

    @Test(expected = NotFoundException.class)
    public void reDeterminationThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenThrow(new NotFoundException("Claim not found by id"));

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        String differentUser = "34234234";

        Claim claim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        countyCourtJudgmentService.save(DATA, EXTERNAL_ID, AUTHORISATION, false);
    }

    @Test(expected = ForbiddenActionException.class)
    public void reDeterminationThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        String differentUser = "34234234";
        UserDetails userDetails = SampleUserDetails.builder().withUserId(differentUser).build();

        Claim claim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(userDetails);

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasResponded() {
        Claim respondedClaim = SampleClaim.builder().withRespondedAt(LocalDateTime.now().minusDays(2)).build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        countyCourtJudgmentService.save(DATA, EXTERNAL_ID, AUTHORISATION, false);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenUserCannotRequestCountyCourtJudgmentYet() {
        Claim respondedClaim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        countyCourtJudgmentService.save(DATA, EXTERNAL_ID, AUTHORISATION, false);
    }


    @Test(expected = ForbiddenActionException.class)
    public void reDeterminationThrowsForbiddenActionExceptionWhenCountyCourtJudgmentIsNotRequestedYet() {
        Claim respondedClaim = SampleClaim.getWithDefaultResponse();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {
        Claim respondedClaim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        countyCourtJudgmentService.save(DATA, EXTERNAL_ID, AUTHORISATION, false);
    }


    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenRedeterminationWasAlreadySubmitted() {
        LocalDateTime submissionDate = LocalDate.of(2018, 4, 26).atStartOfDay();

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
            .withCountyCourtJudgmentRequestedAt(submissionDate)
            .withReDetermination(reDetermination)
            .withReDeterminationRequestedAt(submissionDate)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(claim);

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void saveThrowsExceptionWhenClaimantRepaymentPlanStartDateDoesNotMeetCriteria() {

        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(
                SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build())
            .ccjType(CountyCourtJudgmentType.ADMISSIONS)
            .build();

        Claim invalidClaim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder()
                .paymentIntention(SamplePaymentIntention.builder()
                    .paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(
                        SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now().plusMonths(2)).build())
                    .build())
                .build()
        );

        when(claimService.getClaimByExternalId(eq(invalidClaim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(invalidClaim);

        countyCourtJudgmentService.save(ccj, invalidClaim.getExternalId(), AUTHORISATION, true);

    }

    @Test
    public void saveShouldFinishDefaultCCJRequestSuccessfullyWithInstallmentDateEarlierThanOneMonth() {
        CountyCourtJudgment ccj = SampleCountyCourtJudgment.builder()
            .paymentOption(PaymentOption.INSTALMENTS)
            .repaymentPlan(
                SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build())
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        Claim invalidClaim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder()
                .paymentIntention(SamplePaymentIntention.builder()
                    .paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(
                        SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now().plusMonths(2)).build())
                    .build())
                .build()
        );

        when(claimService.getClaimByExternalId(eq(invalidClaim.getExternalId()), eq(AUTHORISATION)))
            .thenReturn(invalidClaim);

        countyCourtJudgmentService.save(ccj, invalidClaim.getExternalId(), AUTHORISATION, true);
    }
}
