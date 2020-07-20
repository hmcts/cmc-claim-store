package uk.gov.hmcts.cmc.claimstore.controllers.support;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsExceptionLogger;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CountyCourtJudgmentEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimIssuedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rpa.ClaimantResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.DefenceResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.MoreTimeRequestedNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.PaidInFullNotificationService;
import uk.gov.hmcts.cmc.claimstore.rpa.RequestForJudgmentNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoboticsSupportControllerTest {
    private static final GeneratePinResponse PIN_RESPONSE = new GeneratePinResponse("pin", "userid");
    private static final UserDetails USER_DETAILS = new UserDetails(
        "id", "email", "forename", "surname", emptyList());
    @Mock
    private UserService userService;

    @Mock
    private ClaimService claimService;
    @Mock
    private MoreTimeRequestedNotificationService moreTimeRequestedNotificationService;
    @Mock
    private DefenceResponseNotificationService defenceResponseNotificationService;
    @Mock
    private ClaimantResponseNotificationService claimantResponseNotificationService;
    @Mock
    private RequestForJudgmentNotificationService ccjNotificationService;
    @Mock
    private PaidInFullNotificationService paidInFullNotificationService;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;

    @Mock
    private AppInsightsExceptionLogger appInsightsExceptionLogger;
    @Mock
    private DocumentGenerator documentGenerator;

    private RoboticsSupportController controller;

    @Before
    public void setUp() {
        this.controller = new RoboticsSupportController(
            claimService,
            userService,
            moreTimeRequestedNotificationService,
            defenceResponseNotificationService,
            claimantResponseNotificationService,
            ccjNotificationService,
            paidInFullNotificationService,
            responseDeadlineCalculator,
            appInsightsExceptionLogger,
            documentGenerator
        );
        when(userService.authenticateAnonymousCaseWorker())
            .thenReturn(new User("authorisation", USER_DETAILS));
        when(userService.getUserDetails("authorisation")).thenReturn(USER_DETAILS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_ClaimNotifications_NullArgument() {
        controller.rpaClaimNotifications(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_MoreTimeNotifications_NullArgument() {
        controller.rpaMoreTimeNotifications(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_ResponseNotifications_NullArgument() {
        controller.rpaResponseNotifications(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_CCJNotifications_NullArgument() {
        controller.rpaCCJNotifications(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_PIFNotifications_NullArgument() {
        controller.rpaPIFNotifications(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_ClaimantResponseNotifications_NullArgument() {
        controller.rpaClaimantResponseNotifications(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_ClaimNotifications_EmptyArgument() {
        controller.rpaClaimNotifications(emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_MoreTimeNotifications_EmptyArgument() {
        controller.rpaMoreTimeNotifications(emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_ResponseNotifications_EmptyArgument() {
        controller.rpaResponseNotifications(emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_CCJNotifications_EmptyArgument() {
        controller.rpaCCJNotifications(emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_ClaimantResponseNotifications_EmptyArgument() {
        controller.rpaClaimantResponseNotifications(emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRPA_PIFNotifications_EmptyArgument() {
        controller.rpaPIFNotifications(emptyList());
    }

    @Test
    public void testRPA_ClaimNotifications() {
        when(claimService.getClaimByReference("000MC001", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder().withReferenceNumber("000MC001").build()));
        Claim claim2 = SampleClaim.builder().withReferenceNumber("000MC002").build();
        when(claimService.getClaimByReference("000MC002", "authorisation"))
            .thenReturn(Optional.of(claim2));

        when(userService.generatePin(anyString(), anyString())).thenReturn(PIN_RESPONSE);
        when(claimService.linkLetterHolder(eq(claim2), anyString(), anyString())).thenThrow(new RuntimeException(
            "reason"));

        Map<String, String> results = controller.rpaClaimNotifications(asList("000MC001", "000MC002", "000MC003"));

        assertThat(results).contains(
            entry("000MC001", "succeeded"),
            entry("000MC002", "failed: reason"),
            entry("000MC003", "missing")
        );

        verify(documentGenerator).generateForCitizenRPA(any(CitizenClaimIssuedEvent.class));
    }

    @Test
    public void testRPA_MoreTimeNotifications() {
        when(claimService.getClaimByReference("001MC001", "authorisation")).thenReturn(Optional.of(
            SampleClaim.builder().withReferenceNumber("001MC001").withMoreTimeRequested(true).build()));
        when(claimService.getClaimByReference("001MC002", "authorisation")).thenReturn(Optional.of(
            SampleClaim.builder().withReferenceNumber("001MC002").withMoreTimeRequested(false).build()));
        when(claimService.getClaimByReference("001MC003", "authorisation")).thenReturn(Optional.of(
            SampleClaim.builder().withReferenceNumber("001MC003").withMoreTimeRequested(true).build()));

        doNothing()
            .doThrow(new RuntimeException("reason"))
            .when(moreTimeRequestedNotificationService).notifyRobotics(any(MoreTimeRequestedEvent.class));

        Map<String, String> results = controller.rpaMoreTimeNotifications(
            asList("001MC001", "001MC002", "001MC003", "001MC004"));

        assertThat(results).contains(
            entry("001MC001", "succeeded"),
            entry("001MC002", "invalid"),
            entry("001MC003", "failed: reason"),
            entry("001MC004", "missing")
        );

        verify(moreTimeRequestedNotificationService, times(2)).notifyRobotics(any(MoreTimeRequestedEvent.class));
    }

    @Test
    public void testRPA_ResponseNotifications() {
        when(claimService.getClaimByReference("002MC001", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("002MC001")
                .withResponse(SampleResponse.validDefaults()).build()));
        when(claimService.getClaimByReference("002MC002", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("002MC002").build()));
        when(claimService.getClaimByReference("002MC003", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("002MC003")
                .withResponse(SampleResponse.validDefaults()).build()));

        doNothing()
            .doThrow(new RuntimeException("reason"))
            .when(defenceResponseNotificationService).notifyRobotics(any(DefendantResponseEvent.class));

        Map<String, String> results = controller.rpaResponseNotifications(
            asList("002MC001", "002MC002", "002MC003", "002MC004"));

        assertThat(results).contains(
            entry("002MC001", "succeeded"),
            entry("002MC002", "invalid"),
            entry("002MC003", "failed: reason"),
            entry("002MC004", "missing")
        );

        verify(defenceResponseNotificationService, times(2)).notifyRobotics(any(DefendantResponseEvent.class));
    }

    @Test
    public void testRAP_ClaimantResponseNotifications() {
        when(claimService.getClaimByReference("002MC001", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("002MC001")
                .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
                .withClaimantRespondedAt(LocalDateTime.of(2018, 4, 26, 1, 1))
                .build()));
        when(claimService.getClaimByReference("002MC002", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("002MC002").build()));
        when(claimService.getClaimByReference("002MC003", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("002MC003")
                .withClaimantResponse(SampleClaimantResponse.validDefaultAcceptation())
                .withClaimantRespondedAt(LocalDateTime.of(2018, 4, 26, 1, 1))
                .build()));

        doNothing()
            .doThrow(new RuntimeException("reason"))
            .when(claimantResponseNotificationService).notifyRobotics(any(ClaimantResponseEvent.class));

        Map<String, String> results = controller.rpaClaimantResponseNotifications(
            asList("002MC001", "002MC002", "002MC003", "002MC004"));

        assertThat(results).contains(
            entry("002MC001", "succeeded"),
            entry("002MC002", "invalid"),
            entry("002MC003", "failed: reason"),
            entry("002MC004", "missing")
        );

        verify(claimantResponseNotificationService, times(2)).notifyRobotics(any(ClaimantResponseEvent.class));
    }

    @Test
    public void testRPA_CCJNotifications() {
        when(claimService.getClaimByReference("003MC001", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("003MC001")
                .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
                .build()));
        when(claimService.getClaimByReference("003MC002", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("003MC002").build()));
        when(claimService.getClaimByReference("003MC003", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("003MC003")
                .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
                .build()));

        doNothing()
            .doThrow(new RuntimeException("reason"))
            .when(ccjNotificationService).notifyRobotics(any(CountyCourtJudgmentEvent.class));

        Map<String, String> results = controller.rpaCCJNotifications(
            asList("003MC001", "003MC002", "003MC003", "003MC004"));

        assertThat(results).contains(
            entry("003MC001", "succeeded"),
            entry("003MC002", "invalid"),
            entry("003MC003", "failed: reason"),
            entry("003MC004", "missing")
        );

        verify(ccjNotificationService, times(2)).notifyRobotics(any(CountyCourtJudgmentEvent.class));
    }

    @Test
    public void testRPA_PIFNotifications() {
        when(claimService.getClaimByReference("004MC001", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("004MC001")
                .withMoneyReceivedOn(LocalDate.now())
                .build()));
        when(claimService.getClaimByReference("004MC002", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("004MC002").build()));
        when(claimService.getClaimByReference("004MC003", "authorisation"))
            .thenReturn(Optional.of(SampleClaim.builder()
                .withReferenceNumber("004MC003")
                .withMoneyReceivedOn(LocalDate.now())
                .build()));

        doNothing()
            .doThrow(new RuntimeException("reason"))
            .when(paidInFullNotificationService).notifyRobotics(any(PaidInFullEvent.class));

        Map<String, String> results = controller.rpaPIFNotifications(
            asList("004MC001", "004MC002", "004MC003", "004MC004"));

        assertThat(results).contains(
            entry("004MC001", "succeeded"),
            entry("004MC002", "invalid"),
            entry("004MC003", "failed: reason"),
            entry("004MC004", "missing")
        );

        verify(paidInFullNotificationService, times(2)).notifyRobotics(any(PaidInFullEvent.class));
    }
}
