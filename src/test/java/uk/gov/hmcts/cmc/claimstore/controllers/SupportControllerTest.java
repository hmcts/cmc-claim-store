package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.support.SupportController;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseCitizenNotificationsHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SupportControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder().build();

    @Mock
    private ClaimService claimService;

    @Mock
    private UserService userService;

    @Mock
    private DocumentGenerator documentGenerator;

    @Mock
    private MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler;

    @Mock
    private DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler;

    @Mock
    private CCJStaffNotificationHandler ccjStaffNotificationHandler;

    @Mock
    private AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler;

    @Mock
    private DefendantResponseCitizenNotificationsHandler defendantResponseCitizenNotificationsHandler;
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private SupportController controller;

    private Claim sampleClaim;

    @Before
    public void setUp() {
        controller = new SupportController(claimService, userService, documentGenerator,
            moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler,
            defendantResponseCitizenNotificationsHandler
        );
        sampleClaim = SampleClaim.getDefault();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotResendRPANotificationsWhenRequestBodyIsEmpty() {
        List<String> sendList = new ArrayList<>();
        controller.resendRPANotifications(AUTHORISATION, sendList);
    }

    @Test(expected = NotFoundException.class)
    public void shouldNotResendRPANotificationsWhenRequestBodyClaimsDoesNotExistForMultipleClaims() {
        // given
        List<String> sendList = new ArrayList<>();
        sendList.add("000CM001");
        sendList.add("000CM003");
        when(claimService.getClaimByReferenceAnonymous(eq("000CM001"))).thenReturn(Optional.of(sampleClaim));

        // when
        controller.resendRPANotifications(AUTHORISATION, sendList);

        // then
        verify(documentGenerator, never()).generateForCitizenRPA(any());
    }

    @Test
    public void shouldResendRPANotifications() {
        // given
        List<String> sendList = new ArrayList<>();
        sendList.add("000CM001");
        GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", "333");
        given(userService.generatePin(anyString(), eq(AUTHORISATION))).willReturn(pinResponse);

        // when
        when(claimService.getClaimByReferenceAnonymous(eq("000CM001"))).thenReturn(Optional.of(sampleClaim));
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(USER_DETAILS);
        controller.resendRPANotifications(AUTHORISATION, sendList);

        // then
        verify(documentGenerator).generateForCitizenRPA(any());

    }

    @Test
    public void shouldTriggerUploadDocumentWhenDefendantResponseSubmittedInvoked() {
        when(claimService.getClaimByReferenceAnonymous(eq("000CM001"))).thenReturn(Optional.of(sampleClaim));
        controller.resendStaffNotifications("000CM001", "response-submitted", AUTHORISATION);
        verify(defendantResponseCitizenNotificationsHandler).uploadDocument(any(DefendantResponseEvent.class));
    }

    @Test
    public void shouldThrowExceptionIfDefendantResponseSubmittedWhenNoDefendantResponse() {
        exceptionRule.expect(ConflictException.class);
        exceptionRule.expectMessage("Claim 000CM001 does not have associated response");
        when(claimService.getClaimByReferenceAnonymous(eq("000CM001")))
            .thenReturn(Optional.of(SampleClaim.withNoResponse()));
        controller.resendStaffNotifications("000CM001", "response-submitted", AUTHORISATION);

    }

}
