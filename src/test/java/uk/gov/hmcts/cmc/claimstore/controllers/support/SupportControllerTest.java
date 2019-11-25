package uk.gov.hmcts.cmc.claimstore.controllers.support;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ServerErrorException;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullEvent;
import uk.gov.hmcts.cmc.claimstore.events.paidinfull.PaidInFullStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimSubmissionOperationIndicatorRule;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.IntentionToProceedService;
import uk.gov.hmcts.cmc.claimstore.services.MediationReportService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.MediationRequest;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.PartAdmission;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.cmc.domain.models.response.YesNoOption.YES;

@RunWith(MockitoJUnitRunner.class)
public class SupportControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String CLAIM_REFERENCE = "000CM001";
    private static final String RESPONSE_SUBMITTED = "response-submitted";
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

    @Mock
    private DocumentsService documentsService;

    @Mock
    private PostClaimOrchestrationHandler postClaimOrchestrationHandler;

    @Mock
    private MediationReportService mediationReportService;

    @Mock
    private IntentionToProceedService intentionToProceedService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private SupportController controller;

    private Claim sampleClaim;

    @Before
    public void setUp() {
        controller = new SupportController(
            claimService,
            userService,
            documentGenerator,
            moreTimeRequestedStaffNotificationHandler,
            defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler,
            agreementCountersignedStaffNotificationHandler,
            claimantResponseStaffNotificationHandler,
            paidInFullStaffNotificationHandler,
            documentsService,
            postClaimOrchestrationHandler,
            false,
            mediationReportService,
            new ClaimSubmissionOperationIndicatorRule(),
            intentionToProceedService
        );
        sampleClaim = SampleClaim.getDefault();
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);
    }

    @Test
    public void shouldResendStaffNotifications() {
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

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claim-issued");

        verify(documentGenerator).generateForNonRepresentedClaim(any());
    }

    @Test
    public void shouldResendStaffNotificationsForIntentToProceed() {
        ClaimantResponse claimantResponse = ClaimantResponseRejection.builder()
            .buildRejectionWithDirectionsQuestionnaire();

        sampleClaim = SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(PartAdmission.builder().buildWithFreeMediation())
            .withClaimantResponse(claimantResponse)
            .build();

        controller = new SupportController(claimService, userService, documentGenerator,
            moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler,
            claimantResponseStaffNotificationHandler, paidInFullStaffNotificationHandler, documentsService,
            postClaimOrchestrationHandler, true, mediationReportService, new ClaimSubmissionOperationIndicatorRule(),
            intentionToProceedService
        );

        when(claimService.getClaimByReferenceAnonymous(eq(CLAIM_REFERENCE))).thenReturn(Optional.of(sampleClaim));
        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "intent-to-proceed");

        verify(claimantResponseStaffNotificationHandler)
            .notifyStaffWithClaimantsIntentionToProceed(new ClaimantResponseEvent(sampleClaim, AUTHORISATION));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForIntentToProceedIfDQFeatureToggledIsOff() {
        ClaimantResponse claimantResponse = ClaimantResponseRejection.builder()
            .buildRejectionWithDirectionsQuestionnaire();

        sampleClaim = SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(PartAdmission.builder().buildWithFreeMediation())
            .withClaimantResponse(claimantResponse)
            .build();

        controller = new SupportController(claimService, userService, documentGenerator,
            moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler,
            claimantResponseStaffNotificationHandler, paidInFullStaffNotificationHandler, documentsService,
            postClaimOrchestrationHandler, false, mediationReportService, new ClaimSubmissionOperationIndicatorRule(),
            intentionToProceedService
        );

        when(claimService.getClaimByReferenceAnonymous(eq(CLAIM_REFERENCE))).thenReturn(Optional.of(sampleClaim));

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "intent-to-proceed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForIntentToProceedIfClaimantResponseIsNotRejection() {
        sampleClaim = SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withResponse(PartAdmission.builder().buildWithFreeMediation())
            .withClaimantResponse(ClaimantResponseAcceptation.builder().build())
            .build();

        controller = new SupportController(claimService, userService, documentGenerator,
            moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler,
            claimantResponseStaffNotificationHandler, paidInFullStaffNotificationHandler, documentsService,
            postClaimOrchestrationHandler, true, mediationReportService, new ClaimSubmissionOperationIndicatorRule(),
            intentionToProceedService
        );

        when(claimService.getClaimByReferenceAnonymous(eq(CLAIM_REFERENCE))).thenReturn(Optional.of(sampleClaim));

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "intent-to-proceed");
    }

    @Test
    public void shouldThrowExceptionIfDefendantResponseSubmittedWhenNoDefendantResponse() {
        exceptionRule.expect(ConflictException.class);
        exceptionRule.expectMessage("Claim " + CLAIM_REFERENCE + " does not have associated response");

        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE))
            .thenReturn(Optional.of(SampleClaim.withNoResponse()));
        controller.resendStaffNotifications(CLAIM_REFERENCE, RESPONSE_SUBMITTED);
    }

    @Test
    public void shouldResendClaimantResponseNotifications() {
        sampleClaim = SampleClaim.builder()
            .withResponse(PartAdmission.builder().buildWithPaymentOptionImmediately())
            .withClaimantResponse(SampleClaimantResponse.validDefaultRejection())
            .build();

        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claimant-response");

        verify(claimantResponseStaffNotificationHandler).onClaimantResponse(any());
    }

    @Test
    public void shouldResendClaimantResponseNotificationsIfReferToJudgeAndIsBusiness() {
        PaymentIntention paymentIntention = SamplePaymentIntention.immediately();
        Party company = SampleParty.builder().withCorrespondenceAddress(null).company();
        sampleClaim = SampleClaim.builder()
            .withResponse(
                PartAdmission.builder().buildWithPaymentIntentionAndParty(paymentIntention, company)
            )
            .withClaimantResponse(
                ClaimantResponseAcceptation.builder()
                    .buildAcceptationReferToJudgeWithCourtDetermination()
            )
            .build();

        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claimant-response");

        verify(claimantResponseStaffNotificationHandler).onClaimantResponse(any());
    }

    @Test
    public void shouldNotResendClaimantResponseNotificationsIfReferToJudge() {
        sampleClaim = SampleClaim.builder()
            .withResponse(PartAdmission.builder().buildWithPaymentOptionImmediately())
            .withClaimantResponse(
                ClaimantResponseAcceptation.builder()
                    .buildAcceptationReferToJudgeWithCourtDetermination()
            )
            .build();

        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claimant-response");

        verify(claimantResponseStaffNotificationHandler, never()).onClaimantResponse(any());
    }

    @Test
    public void shouldNotResendClaimantResponseNotificationsWhenSettlementAgreementReached() {
        sampleClaim = SampleClaim.builder()
            .withResponse(
                SampleResponse.FullDefence.builder().build()
            )
            .withClaimantResponse(
                ClaimantResponseAcceptation.builder()
                    .buildAcceptationIssueSettlementWithClaimantPaymentIntention()
            )
            .build();

        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claimant-response");

        verify(claimantResponseStaffNotificationHandler, never()).onClaimantResponse(any());
    }

    @Test
    public void shouldThrowExceptionWhenClaimHasNoClaimantResponse() {
        exceptionRule.expect(IllegalArgumentException.class);
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(
            Optional.of(SampleClaim.builder().withClaimantResponse(null).build()));

        controller.resendStaffNotifications(CLAIM_REFERENCE, "claimant-response");
    }

    @Test
    public void shouldResendStaffNotificationForPaidInFull() {
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(
            Optional.of(SampleClaim.builder().withMoneyReceivedOn(LocalDate.now()).build()));
        controller.resendStaffNotifications(CLAIM_REFERENCE, "paid-in-full");
        verify(paidInFullStaffNotificationHandler).onPaidInFullEvent(any(PaidInFullEvent.class));
    }

    @Test
    public void shouldThrowExceptionWhenClaimHasNoMoneyReceivedOnDate() {
        exceptionRule.expect(IllegalArgumentException.class);
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(
            Optional.of(SampleClaim.builder().withMoneyReceivedOn(null).build()));
        controller.resendStaffNotifications(CLAIM_REFERENCE, "paid-in-full");
        verify(paidInFullStaffNotificationHandler, never()).onPaidInFullEvent(any(PaidInFullEvent.class));
    }

    @Test
    public void shouldUploadSealedClaimDocumentWhenAbsent() {
        Claim claim = SampleClaim.getCitizenClaim();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE))
            .thenReturn(Optional.of(claim))
            .thenReturn(Optional.of(SampleClaim.getWithSealedClaimDocument()));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SEALED_CLAIM);
        verify(documentsService).generateDocument(claim.getExternalId(), SEALED_CLAIM, AUTHORISATION);
    }

    @Test
    public void shouldNotUploadSealedClaimDocumentWhenAlreadyExists() {
        Claim claim = SampleClaim.getWithSealedClaimDocument();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SEALED_CLAIM);
        verify(documentsService, never()).generateDocument(claim.getExternalId(), SEALED_CLAIM, AUTHORISATION);
    }

    @Test
    public void shouldThrowServerExceptionWhenSealedClaimUploadFailed() {
        exceptionRule.expect(ServerErrorException.class);
        Claim claim = SampleClaim.getCitizenClaim();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SEALED_CLAIM);
    }

    @Test
    public void shouldUploadClaimIssueReceiptDocumentWhenAbsent() {
        Claim claim = SampleClaim.getCitizenClaim();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE))
            .thenReturn(Optional.of(claim))
            .thenReturn(Optional.of(SampleClaim.getWithClaimIssueReceiptDocument()));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, CLAIM_ISSUE_RECEIPT);
        verify(documentsService).generateDocument(claim.getExternalId(), CLAIM_ISSUE_RECEIPT, AUTHORISATION);
    }

    @Test
    public void shouldNotUploadClaimIssueReceiptDocumentWhenAlreadyExists() {
        Claim claim = SampleClaim.getWithClaimIssueReceiptDocument();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, CLAIM_ISSUE_RECEIPT);
        verify(documentsService, never()).generateDocument(claim.getExternalId(), CLAIM_ISSUE_RECEIPT, AUTHORISATION);
    }

    @Test
    public void shouldThrowServerExceptionWhenClaimIssueReceiptUploadFailed() {
        exceptionRule.expect(ServerErrorException.class);
        Claim claim = SampleClaim.getCitizenClaim();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, CLAIM_ISSUE_RECEIPT);
    }

    @Test
    public void shouldUploadDefendantResponseReceiptDocumentWhenAbsent() {
        Claim claim = SampleClaim.getCitizenClaim();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE))
            .thenReturn(Optional.of(claim))
            .thenReturn(Optional.of(SampleClaim.getWithDefendantResponseReceiptDocument()));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, DEFENDANT_RESPONSE_RECEIPT);
        verify(documentsService).generateDocument(claim.getExternalId(), DEFENDANT_RESPONSE_RECEIPT, AUTHORISATION);
    }

    @Test
    public void shouldNotUploadDefendantResponseReceiptDocumentWhenAlreadyExists() {
        Claim claim = SampleClaim.getWithDefendantResponseReceiptDocument();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, DEFENDANT_RESPONSE_RECEIPT);
        verify(documentsService, never()).generateDocument(claim.getExternalId(), DEFENDANT_RESPONSE_RECEIPT,
            AUTHORISATION);
    }

    @Test
    public void shouldThrowServerExceptionWhenDefendantResponseReceiptUploadFailed() {
        exceptionRule.expect(ServerErrorException.class);
        Claim claim = SampleClaim.getCitizenClaim();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, DEFENDANT_RESPONSE_RECEIPT);
    }

    @Test
    public void shouldUploadSettlementAgreementDocumentWhenAbsent() {
        Claim claim = SampleClaim.getCitizenClaim();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE))
            .thenReturn(Optional.of(claim))
            .thenReturn(Optional.of(SampleClaim.getWithSettlementAgreementDocument()));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SETTLEMENT_AGREEMENT);
        verify(documentsService).generateDocument(claim.getExternalId(), SETTLEMENT_AGREEMENT, AUTHORISATION);
    }

    @Test
    public void shouldNotUploadSettlementAgreementDocumentWhenAlreadyExists() {
        Claim claim = SampleClaim.getWithSettlementAgreementDocument();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SETTLEMENT_AGREEMENT);
        verify(documentsService, never()).generateDocument(claim.getExternalId(), SETTLEMENT_AGREEMENT, AUTHORISATION);
    }

    @Test
    public void shouldThrowServerExceptionWhenSettlementAgreementUploadFailed() {
        exceptionRule.expect(ServerErrorException.class);
        Claim claim = SampleClaim.getCitizenClaim();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SETTLEMENT_AGREEMENT);
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenClaimIsNotFound() {
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.empty());
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Claim " + CLAIM_REFERENCE + " does not exist");
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SEALED_CLAIM);
    }

    @Test
    public void shouldRecoverCitizenClaimIssueOperations() {
        Claim claim = SampleClaim.getDefault();
        when(claimService.getClaimByReference(CLAIM_REFERENCE, AUTHORISATION)).thenReturn(Optional.of(claim));

        controller.recoverClaimIssueOperations(CLAIM_REFERENCE);
        verify(postClaimOrchestrationHandler).citizenIssueHandler(any(CitizenClaimCreatedEvent.class));
    }

    @Test
    public void shouldRecoverRepresentativeClaimIssueOperations() {
        Claim claim = SampleClaim.getDefaultForLegal();
        when(claimService.getClaimByReference(CLAIM_REFERENCE, AUTHORISATION)).thenReturn(Optional.of(claim));

        controller.recoverClaimIssueOperations(CLAIM_REFERENCE);
        verify(postClaimOrchestrationHandler).representativeIssueHandler(any(RepresentedClaimCreatedEvent.class));
    }

    @Test
    public void shouldSendAppInsightIfMediationReportFails() {
        LocalDate mediationSearchDate = LocalDate.of(2019, 07, 07);
        doNothing().when(mediationReportService).sendMediationReport(eq(AUTHORISATION), any());
        controller.sendMediation(AUTHORISATION, new MediationRequest(mediationSearchDate, "Holly@cow.com"));
        verify(mediationReportService, times(1))
            .sendMediationReport(eq(AUTHORISATION), eq(mediationSearchDate));
    }

    @Test
    public void shouldPerformResetOperationForCitizenClaim() {
        Claim claim = SampleClaim.getWithClaimSubmissionOperationIndicators();
        ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators = ClaimSubmissionOperationIndicators
            .builder().build();
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIM_REFERENCE)))
            .thenReturn(Optional.of(claim));
        when(claimService.updateClaimSubmissionOperationIndicators(
            eq(AUTHORISATION),
            eq(claim),
            eq(claimSubmissionOperationIndicators)
        )).thenReturn(claim);
        controller.resetOperation(CLAIM_REFERENCE,
            claimSubmissionOperationIndicators,
            AUTHORISATION);
        verify(claimService).updateClaimSubmissionOperationIndicators(
            eq(AUTHORISATION),
            eq(claim),
            eq(claimSubmissionOperationIndicators));
        verify(postClaimOrchestrationHandler).citizenIssueHandler(any(CitizenClaimCreatedEvent.class));
    }

    @Test
    public void shouldThrowExceptionForResetOperationForCitizenClaim() {
        Claim claim = SampleClaim.getDefault();
        final ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators = ClaimSubmissionOperationIndicators
            .builder()
            .claimIssueReceiptUpload(YES)
            .sealedClaimUpload(YES)
            .bulkPrint(YES)
            .claimantNotification(YES)
            .rpa(YES)
            .defendantNotification(YES)
            .staffNotification(YES)
            .build();
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIM_REFERENCE)))
            .thenReturn(Optional.of(claim));
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("Invalid input. The following indicator(s)[claimantNotification, "
            + "defendantNotification, bulkPrint, rpa, staffNotification, sealedClaimUpload, claimIssueReceiptUpload] "
            + "cannot be set to Yes");
        controller.resetOperation(CLAIM_REFERENCE,
            claimSubmissionOperationIndicators,
            AUTHORISATION);
    }

    @Test
    public void shouldPerformResetOperationForRepresentedClaim() {
        Claim claim = SampleClaim.getDefaultForLegal();
        ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators = ClaimSubmissionOperationIndicators
            .builder().build();
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIM_REFERENCE)))
            .thenReturn(Optional.of(claim));
        when(claimService.updateClaimSubmissionOperationIndicators(
            eq(AUTHORISATION),
            eq(claim),
            eq(claimSubmissionOperationIndicators)
        )).thenReturn(claim);
        controller.resetOperation(CLAIM_REFERENCE,
            claimSubmissionOperationIndicators,
            AUTHORISATION);
        verify(claimService).updateClaimSubmissionOperationIndicators(
            eq(AUTHORISATION),
            eq(claim),
            eq(claimSubmissionOperationIndicators));
        verify(postClaimOrchestrationHandler).representativeIssueHandler(any(RepresentedClaimCreatedEvent.class));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenResetClaimSubmissionIndicator() {
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("Authorisation is required");
        controller.resetOperation(CLAIM_REFERENCE,
            ClaimSubmissionOperationIndicators
                .builder().build(),
            "");
    }

    @Test
    public void shouldPerformIntentionToProceedCheckWithDatetime() {
        final LocalDateTime localDateTime = LocalDateTime.of(2019, 1, 1, 1, 1, 1);
        final String auth = "auth";
        final UserDetails userDetails = new UserDetails("id", null, null, null, null);
        final User user = new User(null, userDetails);
        when(userService.getUser(auth)).thenReturn(user);

        controller.checkClaimsPastIntentionToProceedDeadline(auth, localDateTime);

        verify(intentionToProceedService).checkClaimsPastIntentionToProceedDeadline(localDateTime, user);
    }

    @Test
    public void shouldPerformIntentionToProceedCheckWithNullDatetime() {
        final String auth = "auth";
        final UserDetails userDetails = new UserDetails("id", null, null, null, null);
        final User user = new User(null, userDetails);
        when(userService.getUser(auth)).thenReturn(user);

        controller.checkClaimsPastIntentionToProceedDeadline(auth, null);

        verify(intentionToProceedService).checkClaimsPastIntentionToProceedDeadline(notNull(), eq(user));
    }

}
