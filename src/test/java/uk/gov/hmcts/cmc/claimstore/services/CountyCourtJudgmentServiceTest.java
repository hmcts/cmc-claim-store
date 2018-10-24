package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.DEFAULT;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentServiceTest {

    private static final String AUTHORISATION = "Bearer: aaa";

    private CountyCourtJudgmentService countyCourtJudgmentService;

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
            appInsights);
    }

    @Test
    public void saveShouldFinishCCJRequestSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2)).build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(USER_ID, ccjByDefault, EXTERNAL_ID, AUTHORISATION);

        verify(eventProducer, once()).createCountyCourtJudgmentEvent(
            any(Claim.class),
            any(),
            eq(DEFAULT)
        );
        verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any());
        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_REQUESTED), eq(claim.getReferenceNumber()));
    }

    @Test
    public void saveShouldFinishCCJByAdmissionSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        CountyCourtJudgment ccjByAdmission = SampleCountyCourtJudgment.builder().ccjType(ADMISSIONS).build();
        countyCourtJudgmentService.save(USER_ID, ccjByAdmission, EXTERNAL_ID, AUTHORISATION);

        verify(eventProducer, once()).createCountyCourtJudgmentEvent(
            any(Claim.class), any(),
            eq(ADMISSIONS)
        );
        verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any());
        verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_REQUESTED), eq(claim.getReferenceNumber()));
    }

    @Test(expected = NotFoundException.class)
    public void saveThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenThrow(new NotFoundException("Claim not found by id"));

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(USER_ID, ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        String differentUser = "34234234";

        Claim claim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(differentUser, ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasResponded() {
        Claim respondedClaim = SampleClaim.builder().withRespondedAt(LocalDateTime.now().minusDays(2)).build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(USER_ID, ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenUserCannotRequestCountyCourtJudgmentYet() {
        Claim respondedClaim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(USER_ID, ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {
        Claim respondedClaim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);
        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(USER_ID, ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }
}
