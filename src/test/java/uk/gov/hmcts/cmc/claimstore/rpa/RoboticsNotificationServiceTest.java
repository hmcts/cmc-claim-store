package uk.gov.hmcts.cmc.claimstore.rpa;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsExceptionLogger;
import uk.gov.hmcts.cmc.claimstore.documents.SealedClaimPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RoboticsNotificationServiceImpl;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoboticsNotificationServiceTest {

    RoboticsNotificationServiceImpl roboticsNotificationService;
    @Mock
    private UserService userService;
    @Mock
    private ClaimService claimService;
    @Mock
    private MoreTimeRequestedNotificationService moreTimeRequestedNotificationService;
    @Mock
    private DefenceResponseNotificationService defenceResponseNotificationService;
    @Mock
    private RequestForJudgmentNotificationService ccjNotificationService;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private AppInsightsExceptionLogger appInsightsExceptionLogger;
    @Mock
    private ClaimIssuedNotificationService rpaNotificationService;
    @Mock
    private SealedClaimPdfService sealedClaimPdfService;
    @Mock
    private PaidInFullNotificationService paidInFullNotificationService;

    private static final String FORE_NAME = "forename";
    private static final String SURNAME = "surname";
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder()
        .withForename(FORE_NAME)
        .withSurname(SURNAME)
        .build();
    private static final String REFERENCE_NUMBER = "000MC001";
    private static final GeneratePinResponse PIN_RESPONSE = new GeneratePinResponse("pin", "userid");
    private static final String RPA_STATE_SUCCEEDED = "succeeded";
    private static final String RPA_STATE_FAILED = "failed: reason";
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};

    @Before
    public void setup() {
        roboticsNotificationService = new RoboticsNotificationServiceImpl(claimService,
            userService, moreTimeRequestedNotificationService, defenceResponseNotificationService,
            ccjNotificationService, paidInFullNotificationService,
            responseDeadlineCalculator, appInsightsExceptionLogger, rpaNotificationService,
            sealedClaimPdfService);

        when(userService.authenticateAnonymousCaseWorker()).thenReturn(SampleUser.getDefault());
        doNothing().when(rpaNotificationService).notifyRobotics(any(Claim.class), anyList());
        when(sealedClaimPdfService.createPdf(any(Claim.class)))
            .thenReturn(new PDF("name", PDF_CONTENT, ClaimDocumentType.SEALED_CLAIM));
    }

    @Test
    public void shouldResetRpaForValidClaimEvent() {
        when(userService.generatePin(anyString(), anyString())).thenReturn(PIN_RESPONSE);
        when(userService.getUserDetails(anyString())).thenReturn(USER_DETAILS);
        Claim claim2 = SampleClaim.builder().withReferenceNumber(REFERENCE_NUMBER).build();
        when(claimService.getClaimByReference(anyString(), anyString()))
            .thenReturn(Optional.of(claim2));
        String response = roboticsNotificationService.rpaClaimNotification(REFERENCE_NUMBER);
        assertThat(response, is(RPA_STATE_SUCCEEDED));
    }

    @Test
    public void shouldResetRpaForValidResponseEvent() {
        when(claimService.getClaimByReference(anyString(), anyString()))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber(REFERENCE_NUMBER)
                .withResponse(SampleResponse.validDefaults()).build()));
        doNothing()
            .when(defenceResponseNotificationService).notifyRobotics(any(DefendantResponseEvent.class));
        String response = roboticsNotificationService.rpaResponseNotifications(REFERENCE_NUMBER);
        assertThat(response, is(RPA_STATE_SUCCEEDED));
        verify(defenceResponseNotificationService, times(1))
            .notifyRobotics(any(DefendantResponseEvent.class));
    }

    @Test
    public void shouldResetRpaForValidPaidInFullEvent() {
        when(claimService.getClaimByReference(anyString(), anyString()))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber(REFERENCE_NUMBER)
                .withMoneyReceivedOn(LocalDate.now())
                .build()));
        String response = roboticsNotificationService.rpaPIFNotifications(REFERENCE_NUMBER);
        assertThat(response, is(RPA_STATE_SUCCEEDED));
        verify(paidInFullNotificationService, times(1))
            .notifyRobotics(any(PaidInFullEvent.class));
    }

    @Test
    public void shouldResetRpaForValidCcjEvent() {
        when(claimService.getClaimByReference(anyString(), anyString()))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber(REFERENCE_NUMBER)
                .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
                .build()));
        doNothing()
            .when(ccjNotificationService).notifyRobotics(any(CountyCourtJudgmentEvent.class));
        String response = roboticsNotificationService.rpaCCJNotifications(REFERENCE_NUMBER);
        assertThat(response, is(RPA_STATE_SUCCEEDED));
        verify(ccjNotificationService, times(1))
            .notifyRobotics(any(CountyCourtJudgmentEvent.class));
    }

    @Test
    public void shouldResetRpaForValidMoreTimeEvent() {
        when(claimService.getClaimByReference(any(), any()))
            .thenReturn(Optional.of(
                SampleClaim.builder().withReferenceNumber(REFERENCE_NUMBER)
                    .withMoreTimeRequested(true).build()));
        doNothing()
            .doThrow(new RuntimeException("reason"))
            .when(moreTimeRequestedNotificationService).notifyRobotics(any(MoreTimeRequestedEvent.class));
        String response = roboticsNotificationService.rpaMoreTimeNotifications(REFERENCE_NUMBER);
        assertThat(response, is(RPA_STATE_SUCCEEDED));
        verify(moreTimeRequestedNotificationService, times(1))
            .notifyRobotics(any(MoreTimeRequestedEvent.class));
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidReferencePassedForClaimEvent() {
        roboticsNotificationService.rpaClaimNotification(null);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidReferencePassedForResponseEvent() {
        roboticsNotificationService.rpaResponseNotifications(null);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidReferencePassedForPidEvent() {
        roboticsNotificationService.rpaPIFNotifications(null);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidReferencePassedForCcjEvent() {
        roboticsNotificationService.rpaCCJNotifications(null);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidReferencePassedForMoreTimeEvent() {
        roboticsNotificationService.rpaMoreTimeNotifications(null);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowExceptionWhenInvalidEventTypePassed() {
        when(claimService.getClaimByReference(anyString(), anyString()))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber(REFERENCE_NUMBER).build()));
        roboticsNotificationService.rpaCCJNotifications(REFERENCE_NUMBER);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowExceptionWhenNoClaimForReferenceFound() {
        when(claimService.getClaimByReference(anyString(), anyString()))
            .thenReturn(Optional.of(SampleClaim.builder().build()));
        roboticsNotificationService.rpaCCJNotifications(REFERENCE_NUMBER);
    }
}


