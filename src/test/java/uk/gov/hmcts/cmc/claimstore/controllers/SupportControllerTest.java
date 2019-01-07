package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.support.SupportController;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SupportControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";

    @Mock
    private ClaimService claimService;

    @Mock
    private UserService userService;

    @Mock
    private DocumentGenerator documentGenerator;

    @Mock
    MoreTimeRequestedStaffNotificationHandler moreTimeRequestedStaffNotificationHandler;

    @Mock
    DefendantResponseStaffNotificationHandler defendantResponseStaffNotificationHandler;

    @Mock
    CCJStaffNotificationHandler ccjStaffNotificationHandler;

    @Mock
    AgreementCountersignedStaffNotificationHandler agreementCountersignedStaffNotificationHandler;

    private SupportController controller;

    private Claim sampleClaim;


    @Before
    public void setup() {
        controller = new SupportController(claimService, userService, documentGenerator,
            moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler
        );
        sampleClaim = SampleClaim.getDefault();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotResendRPANotificationsWhenRequestBodyIsEmpty() {
        controller.resendRPANotifications(AUTHORISATION, eq(singletonList("")));
    }

    @Test(expected = NotFoundException.class)
    public void shouldNotResendRPANotificationsWhenRequestBodyClaimsDoNotExist() {
        // given
        when(claimService.getClaimByReferenceAnonymous(eq("000CM001"))).thenReturn(Optional.of(sampleClaim));

        // when
        controller.resendRPANotifications(AUTHORISATION, eq(singletonList("000CM003")));
    }

    @Test(expected = NotFoundException.class)
    public void shouldNotResendRPANotificationsWhenRequestBodyClaimsDoesNotExistForMutlipleClaims() {
        // given
        List<String> sendList = new ArrayList<>();
        sendList.add("000CM001");
        sendList.add("000CM003");

        when(claimService.getClaimByReferenceAnonymous(eq("000CM001"))).thenReturn(Optional.of(sampleClaim));

        // when
        controller.resendRPANotifications(AUTHORISATION, sendList);

        verify(documentGenerator, never()).generateForCitizenRPA(any());
    }

    @Test
    public void shouldResendRPANotifications() {
        // given
        when(claimService.getClaimByReferenceAnonymous(eq("000CM001"))).thenReturn(Optional.of(sampleClaim));

        // when
        controller.resendRPANotifications(eq(AUTHORISATION), eq(singletonList("000CM001")));

        // then
        verify(userService).authenticateUser(any(), any());
        verify(documentGenerator).generateForCitizenRPA(any());

    }

}
