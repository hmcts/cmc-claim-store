package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectOrgPaymentPlanStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.ClaimantRejectionStaffNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.StatesPaidStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseStaffNotificationHandlerTest {

    private static final LocalDateTime NOW_IN_LOCAL_ZONE = LocalDateTimeFactory.nowInLocalZone();
    private static final String AUTHORISATION = "Bearer authorisation";

    @Mock
    private StatesPaidStaffNotificationService statesPaidStaffNotificationService;

    @Mock
    private ClaimantRejectionStaffNotificationService claimantRejectionStaffNotificationService;

    @Mock
    private ClaimantRejectOrgPaymentPlanStaffNotificationService rejectOrgPaymentPlanStaffNotificationService;

    @Mock
    private CCJStaffNotificationHandler ccjStaffNotificationHandler;

    private ClaimantResponseStaffNotificationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClaimantResponseStaffNotificationHandler(
            statesPaidStaffNotificationService,
            claimantRejectionStaffNotificationService,
            rejectOrgPaymentPlanStaffNotificationService,
            ccjStaffNotificationHandler
        );
    }

    @Nested
    @DisplayName("Intend to proceed responses")
    class IntendToProceed {
        @Test
        void shouldNotifyStaffWhenClaimantIntendsToProceed() {
            Claim claim = Claim.builder()
                .claimantRespondedAt(NOW_IN_LOCAL_ZONE)
                .claimantResponse(
                    SampleClaimantResponse.ClaimantResponseRejection.builder()
                        .buildRejectionWithDirectionsQuestionnaire()
                )
                .build();
            ClaimantResponseEvent event = new ClaimantResponseEvent(claim, AUTHORISATION);

            handler.notifyStaffWithClaimantsIntentionToProceed(event);

            verify(claimantRejectionStaffNotificationService)
                .notifyStaffWithClaimantsIntentionToProceed(event.getClaim());
        }

        @Test
        void shouldNotNotifyStaffWhenNotAnIntentToProceedResponse() {
            Claim claim = Claim.builder()
                .claimantRespondedAt(NOW_IN_LOCAL_ZONE)
                .claimantResponse(SampleClaimantResponse.validDefaultAcceptation())
                .build();
            ClaimantResponseEvent event = new ClaimantResponseEvent(claim, AUTHORISATION);

            handler.notifyStaffWithClaimantsIntentionToProceed(event);

            verifyNoInteractions(claimantRejectionStaffNotificationService);
        }
    }

    @Nested
    @DisplayName("Other claimant responses")
    class OtherResponses {
        @Test
        void notifyStaffClaimantResponseStatesPaidSubmittedFor() {
            ClaimantResponseEvent event = new ClaimantResponseEvent(
                SampleClaim.getClaimFullDefenceStatesPaidWithAcceptation(), AUTHORISATION);
            handler.onClaimantResponse(event);

            verify(statesPaidStaffNotificationService)
                .notifyStaffClaimantResponseStatesPaidSubmittedFor(event.getClaim());
        }

        @Test
        void notifyStaffClaimantResponseRejectedPartAdmission() {
            ClaimantResponseEvent event = new ClaimantResponseEvent(
                SampleClaim.getWithClaimantResponseRejectionForPartAdmissionAndMediation(), AUTHORISATION
            );
            handler.onClaimantResponse(event);

            verify(claimantRejectionStaffNotificationService)
                .notifyStaffClaimantRejectPartAdmission(event.getClaim());
        }

        @Test
        void throwExceptionWhenResponseNotPresent() {
            ClaimantResponseEvent event = new ClaimantResponseEvent(SampleClaim.builder().build(), AUTHORISATION);

            assertThrows(IllegalArgumentException.class,
                () -> handler.onClaimantResponse(event));

            verifyNoInteractions(statesPaidStaffNotificationService, claimantRejectionStaffNotificationService);
        }

        @Test
        void shouldThrowExceptionWhenClaimantResponseNotPresent() {
            ClaimantResponseEvent event = new ClaimantResponseEvent(SampleClaim.builder().build(), AUTHORISATION);

            assertThrows(IllegalArgumentException.class,
                () -> handler.notifyStaffWithClaimantsIntentionToProceed(event));

            verifyNoInteractions(statesPaidStaffNotificationService, claimantRejectionStaffNotificationService);
        }

        @Test
        void shouldNotifyStaffOfReferToJudgeForBusiness() {
            Claim claim = Claim.builder()
                .respondedAt(NOW_IN_LOCAL_ZONE)
                .response(SampleResponse.FullAdmission.builder()
                    .withDefendantDetails(SampleParty.builder().company())
                    .build()
                )
                .claimantRespondedAt(NOW_IN_LOCAL_ZONE)
                .claimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                        .buildAcceptationReferToJudgeWithCourtDetermination())
                .build();
            ClaimantResponseEvent event = new ClaimantResponseEvent(claim, AUTHORISATION);

            handler.onClaimantResponse(event);

            assertAll(
                () -> verify(rejectOrgPaymentPlanStaffNotificationService)
                    .notifyStaffClaimantRejectOrganisationPaymentPlan(claim),
                () -> verifyNoInteractions(ccjStaffNotificationHandler)
            );
        }

        @Test
        void shouldNotifyStaffOfReferToJudgeForIndividual() {
            Claim claim = Claim.builder()
                .respondedAt(NOW_IN_LOCAL_ZONE)
                .response(SampleResponse.FullAdmission.builder()
                    .withDefendantDetails(SampleParty.builder().soleTrader())
                    .build()
                )
                .claimantRespondedAt(NOW_IN_LOCAL_ZONE)
                .claimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation.builder()
                    .buildAcceptationReferToJudgeWithCourtDetermination())
                .build();
            ClaimantResponseEvent event = new ClaimantResponseEvent(claim, AUTHORISATION);

            handler.onClaimantResponse(event);

            assertAll(
                () -> verify(ccjStaffNotificationHandler).onInterlocutoryJudgmentEvent(any()),
                () -> verifyNoInteractions(rejectOrgPaymentPlanStaffNotificationService)
            );
        }
    }
}
