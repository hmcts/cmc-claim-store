package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUser;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimIssuedStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class NotifyStaffOperationServiceTest {
    public static final Claim CLAIM = SampleClaim.getDefault();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final PDF pinLetterClaim = new PDF("0000-pin", "test".getBytes(), DEFENDANT_PIN_LETTER);
    public static final PDF sealedClaim = new PDF("0000-sealed-claim", "test".getBytes(), SEALED_CLAIM);

    private NotifyStaffOperationService notifyStaffOperationService;

    @Mock
    private ClaimIssuedStaffNotificationService claimIssuedStaffNotificationService;
    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;
    @Mock
    private UserService userService;

    @Before
    public void before() {
        notifyStaffOperationService = new NotifyStaffOperationService(
            claimIssuedStaffNotificationService,
            eventsStatusService,
            userService
        );

    }

    @Test
    public void shouldNotifyStaffForCitizenProcess() {
        //given
        User citizen = SampleUser.builder()
            .withAuthorisation(AUTHORISATION)
            .withUserDetails(SampleUserDetails.builder().withRoles("citizen").build())
            .build();

        given(userService.getUser(AUTHORISATION)).willReturn(citizen);
        //when
        notifyStaffOperationService.notify(CLAIM, AUTHORISATION, pinLetterClaim, sealedClaim);

        //verify
        verify(claimIssuedStaffNotificationService).notifyStaffOfClaimIssue(
            eq(CLAIM),
            eq(ImmutableList.of(pinLetterClaim, sealedClaim))
        );

        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION), eq(CLAIM),
            eq(CaseEvent.PIN_GENERATION_OPERATIONS));
    }

    @Test
    public void shouldNotifyStaffForRepresentativeProcess() {
        //given
        User citizen = SampleUser.builder()
            .withAuthorisation(AUTHORISATION)
            .withUserDetails(SampleUserDetails.builder().withRoles("solicitor").build())
            .build();

        given(userService.getUser(AUTHORISATION)).willReturn(citizen);
        //when
        notifyStaffOperationService.notify(CLAIM, AUTHORISATION, pinLetterClaim, sealedClaim);

        //verify
        verify(claimIssuedStaffNotificationService).notifyStaffOfClaimIssue(
            eq(CLAIM),
            eq(ImmutableList.of(pinLetterClaim, sealedClaim))
        );

        verify(eventsStatusService).updateClaimOperationCompletion(eq(AUTHORISATION),
            eq(CLAIM.getId()),
            eq(CLAIM.getClaimSubmissionOperationIndicators().toBuilder()
                .defendantNotification(null)
                .staffNotification(YES)
                .bulkPrint(YES)
                .build()),
            eq(CaseEvent.PIN_GENERATION_OPERATIONS));
    }
}
