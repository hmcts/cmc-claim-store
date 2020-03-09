package uk.gov.hmcts.cmc.claimstore.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StaffEmailSupportControllerTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String CLAIM_REFERENCE = "000CM001";
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder().build();
    private static final User USER = new User(AUTHORISATION, USER_DETAILS);

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
    private ClaimantResponseStaffNotificationHandler claimantResponseStaffNotificationHandler;

    @Mock
    private PaidInFullStaffNotificationHandler paidInFullStaffNotificationHandler;

    private StaffEmailSupportController controller;

    private Claim sampleClaim;

    @BeforeEach
    void setUp() {
        controller = new StaffEmailSupportController(
            claimService,
            userService,
            documentGenerator,
            moreTimeRequestedStaffNotificationHandler,
            defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler,
            agreementCountersignedStaffNotificationHandler,
            claimantResponseStaffNotificationHandler,
            paidInFullStaffNotificationHandler
        );
        sampleClaim = SampleClaim.getDefault();
    }

    @Nested
    @DisplayName("Test notifications")
    class NotificationTests {

        @BeforeEach
        void setUpAnonymousCaseworker() {
            when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);
        }

        @Nested
        @DisplayName("Staff Notifications Tests")
        class StaffNotifications {
            @Test
            void shouldResendStaffNotifications() {
                sampleClaim = SampleClaim.builder()
                    .withClaimData(SampleClaimData.submittedByClaimant())
                    .withDefendantId(null)
                    .build();

                String letterHolderId = "333";
                GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", letterHolderId);
                given(userService.generatePin(anyString(), eq(AUTHORISATION))).willReturn(pinResponse);

                when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));
                when(userService.getUserDetails(AUTHORISATION)).thenReturn(USER_DETAILS);

                when(claimService.linkLetterHolder(sampleClaim, letterHolderId, AUTHORISATION)).thenReturn(sampleClaim);

                controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claim");

                verify(documentGenerator).generateForNonRepresentedClaim(any());
            }

            @Test
            void shouldResendStaffNotificationsForIntentToProceed() {
                ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseRejection.builder()
                    .buildRejectionWithDirectionsQuestionnaire();

                sampleClaim = SampleClaim.builder()
                    .withClaimData(SampleClaimData.submittedByClaimant())
                    .withResponse(SampleResponse.PartAdmission.builder().buildWithFreeMediation())
                    .withClaimantResponse(claimantResponse)
                    .build();

                controller = new StaffEmailSupportController(claimService, userService, documentGenerator,
                    moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
                    ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler,
                    claimantResponseStaffNotificationHandler, paidInFullStaffNotificationHandler
                );

                when(claimService.getClaimByReferenceAnonymous(eq(CLAIM_REFERENCE)))
                    .thenReturn(Optional.of(sampleClaim));

                controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "intent-to-proceed");

                verify(claimantResponseStaffNotificationHandler)
                    .notifyStaffWithClaimantsIntentionToProceed(new ClaimantResponseEvent(sampleClaim, AUTHORISATION));
            }

            @Test
            void shouldResendStaffNotificationForPaidInFull() {
                when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(
                    Optional.of(SampleClaim.builder().withMoneyReceivedOn(LocalDate.now()).build()));

                controller.resendStaffNotifications(CLAIM_REFERENCE, "paid-in-full");
                verify(paidInFullStaffNotificationHandler).onPaidInFullEvent(any(PaidInFullEvent.class));
            }
        }

    }

    @Nested
    @DisplayName("Claimant Response Tests")
    class ClaimantResponseTests {

        @BeforeEach
        void setUpAnonymousCaseworker() {
            when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);
        }

        @Test
        void shouldResendClaimantResponseNotifications() {
            sampleClaim = SampleClaim.builder()
                .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionImmediately())
                .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
                .build();

            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

            controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claimant-response");

            verify(claimantResponseStaffNotificationHandler).onClaimantResponse(any());
        }

        @Test
        void shouldResendClaimantResponseNotificationsIfReferToJudgeAndIsBusiness() {
            PaymentIntention paymentIntention = SamplePaymentIntention.immediately();
            Party company = SampleParty.builder().withCorrespondenceAddress(null).company();
            sampleClaim = SampleClaim.builder()
                .withResponse(
                    SampleResponse.PartAdmission.builder().buildWithPaymentIntentionAndParty(paymentIntention, company)
                )
                .withClaimantResponse(
                    SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                        .buildAcceptationReferToJudgeWithCourtDetermination()
                )
                .build();

            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

            controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claimant-response");

            verify(claimantResponseStaffNotificationHandler).onClaimantResponse(any());
        }

        @Test
        void shouldResendClaimantResponseNotificationsIfReferToJudge() {
            sampleClaim = SampleClaim.builder()
                .withResponse(SampleResponse.PartAdmission.builder().buildWithPaymentOptionImmediately())
                .withClaimantResponse(
                    SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                        .buildAcceptationReferToJudgeWithCourtDetermination()
                )
                .build();

            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

            controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claimant-response");

            verify(claimantResponseStaffNotificationHandler).onClaimantResponse(any());
        }

        @Test
        void shouldNotResendClaimantResponseNotificationsWhenSettlementAgreementReached() {
            sampleClaim = SampleClaim.builder()
                .withResponse(
                    SampleResponse.FullDefence.builder().build()
                )
                .withClaimantResponse(
                    SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                        .buildAcceptationIssueSettlementWithClaimantPaymentIntention()
                )
                .build();

            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

            controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claimant-response");

            verify(claimantResponseStaffNotificationHandler, never()).onClaimantResponse(any());
        }

        @Test
        void shouldThrowForIntentToProceedIfClaimantResponseIsNotRejection() {
            sampleClaim = SampleClaim.builder()
                .withClaimData(SampleClaimData.submittedByClaimant())
                .withResponse(SampleResponse.PartAdmission.builder().buildWithFreeMediation())
                .withClaimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation.builder().build())
                .build();

            controller = new StaffEmailSupportController(claimService, userService, documentGenerator,
                moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
                ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler,
                claimantResponseStaffNotificationHandler, paidInFullStaffNotificationHandler
            );

            when(claimService.getClaimByReferenceAnonymous(eq(CLAIM_REFERENCE)))
                .thenReturn(Optional.of(sampleClaim));

            assertThrows(IllegalArgumentException.class,
                () -> controller.resendStaffNotifications(
                    sampleClaim.getReferenceNumber(),
                    "intent-to-proceed"));
        }

        @Test
        void shouldThrowExceptionWhenClaimHasNoClaimantResponse() {
            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(
                Optional.of(SampleClaim.builder().withClaimantResponse(null).build()));

            assertThrows(IllegalArgumentException.class,
                () -> controller.resendStaffNotifications(CLAIM_REFERENCE, "claimant-response"));
        }

        @Test
        void shouldThrowExceptionWhenClaimHasNoMoneyReceivedOnDate() {
            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(
                Optional.of(SampleClaim.builder().withMoneyReceivedOn(null).build()));

            assertThrows(IllegalArgumentException.class,
                () -> controller.resendStaffNotifications(CLAIM_REFERENCE, "paid-in-full"));

            verify(paidInFullStaffNotificationHandler, never()).onPaidInFullEvent(any(PaidInFullEvent.class));
        }
    }
}
