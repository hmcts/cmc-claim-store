package uk.gov.hmcts.cmc.claimstore.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimSubmissionOperationIndicatorRule;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.IntentionToProceedService;
import uk.gov.hmcts.cmc.claimstore.services.MediationReportService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.MediationRequest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@ExtendWith(MockitoExtension.class)
class SupportControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String CLAIM_REFERENCE = "000CM001";

    @Mock
    private ClaimService claimService;

    @Mock
    private UserService userService;

    @Mock
    private DocumentsService documentsService;

    @Mock
    private PostClaimOrchestrationHandler postClaimOrchestrationHandler;

    @Mock
    private MediationReportService mediationReportService;

    @Mock
    private IntentionToProceedService intentionToProceedService;

    private SupportController controller;

    @BeforeEach
    void setUp() {
        controller = new SupportController(
            claimService,
            userService,
            documentsService,
            postClaimOrchestrationHandler,
            mediationReportService,
            new ClaimSubmissionOperationIndicatorRule(),
            intentionToProceedService
        );
    }

    @Nested
    @DisplayName("Intention to proceed deadline")
    class IntentionToProceedDeadlineTests {

        @Test
        void shouldPerformIntentionToProceedCheckWithDatetime() {
            final LocalDateTime localDateTime
                = LocalDateTime.of(2019, 1, 1, 1, 1, 1);
            final String auth = "auth";
            final UserDetails userDetails
                = new UserDetails("id", null, null, null, null);
            final User user = new User(null, userDetails);
            when(userService.getUser(auth)).thenReturn(user);

            controller.checkClaimsPastIntentionToProceedDeadline(auth, localDateTime);

            verify(intentionToProceedService).checkClaimsPastIntentionToProceedDeadline(localDateTime, user);
        }

        @Test
        void shouldPerformIntentionToProceedCheckWithNullDatetime() {
            final String auth = "auth";
            final UserDetails userDetails
                = new UserDetails("id", null, null, null, null);
            final User user = new User(null, userDetails);
            when(userService.getUser(auth)).thenReturn(user);

            controller.checkClaimsPastIntentionToProceedDeadline(auth, null);

            verify(intentionToProceedService).checkClaimsPastIntentionToProceedDeadline(notNull(), eq(user));
        }

    }

    @Nested
    @DisplayName("Reset operation")
    class ResetOperationIndicatorsTests {

        @Test
        void shouldPerformResetOperationForRepresentedClaim() {
            Claim claim = SampleClaim.getDefaultForLegal();
            ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators =
                ClaimSubmissionOperationIndicators.builder().build();
            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
            when(claimService
                .updateClaimSubmissionOperationIndicators(AUTHORISATION, claim, claimSubmissionOperationIndicators))
                .thenReturn(claim);
            controller.resetOperation(CLAIM_REFERENCE, claimSubmissionOperationIndicators, AUTHORISATION);
            verify(claimService)
                .updateClaimSubmissionOperationIndicators(AUTHORISATION, claim, claimSubmissionOperationIndicators);
            verify(postClaimOrchestrationHandler)
                .representativeIssueHandler(any(RepresentedClaimCreatedEvent.class));
        }

        @Test
        void shouldThrowExceptionForResetOperationForCitizenClaim() {
            Claim claim = SampleClaim.getDefault();
            ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators =
                ClaimSubmissionOperationIndicators.builder()
                    .claimIssueReceiptUpload(YES)
                    .sealedClaimUpload(YES)
                    .bulkPrint(YES)
                    .claimantNotification(YES)
                    .rpa(YES)
                    .defendantNotification(YES)
                    .staffNotification(YES)
                    .build();
            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
            assertThrows(BadRequestException.class,
                () -> controller.resetOperation(
                    CLAIM_REFERENCE,
                    claimSubmissionOperationIndicators,
                    AUTHORISATION
                ),
                "Invalid input. The following indicator(s)[claimantNotification, "
                    + "defendantNotification, bulkPrint, rpa, staffNotification, "
                    + "sealedClaimUpload, claimIssueReceiptUpload] "
                    + "cannot be set to Yes"
            );
        }

        @Test
        void shouldThrowBadRequestExceptionWhenResetClaimSubmissionIndicator() {
            assertThrows(BadRequestException.class,
                () -> controller.resetOperation(
                    CLAIM_REFERENCE,
                    ClaimSubmissionOperationIndicators.builder().build(),
                    ""
                ),
                "Authorisation is required");
        }

        @Test
        void shouldPerformResetOperationForCitizenClaim() {
            Claim claim = SampleClaim.getWithClaimSubmissionOperationIndicators();
            ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators
                = ClaimSubmissionOperationIndicators.builder().build();
            when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
            when(claimService.updateClaimSubmissionOperationIndicators(
                AUTHORISATION,
                claim,
                claimSubmissionOperationIndicators
            )).thenReturn(claim);
            controller.resetOperation(CLAIM_REFERENCE, claimSubmissionOperationIndicators, AUTHORISATION);
            verify(claimService)
                .updateClaimSubmissionOperationIndicators(AUTHORISATION, claim, claimSubmissionOperationIndicators);
            verify(postClaimOrchestrationHandler)
                .citizenIssueHandler(any(CitizenClaimCreatedEvent.class));
        }

    }

    @Nested
    @DisplayName("App Insights Tests")
    class ApplicationInsightsTests {
        @Test
        void shouldSendAppInsightIfMediationReportFails() {
            LocalDate mediationSearchDate = LocalDate.of(2019, 7, 7);
            MediationRequest mediationRequest = new MediationRequest(mediationSearchDate, "Holly@cow.com");
            doNothing().when(mediationReportService).sendMediationReport(eq(AUTHORISATION), any());

            controller.sendMediation(AUTHORISATION, mediationRequest);

            verify(mediationReportService).sendMediationReport(AUTHORISATION, mediationSearchDate);
        }
    }
}
