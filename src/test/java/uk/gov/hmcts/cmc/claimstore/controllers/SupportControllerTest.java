package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.controllers.support.SupportController;
import uk.gov.hmcts.cmc.claimstore.events.ccj.CCJStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claim.CitizenClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.events.claim.DocumentGenerator;
import uk.gov.hmcts.cmc.claimstore.events.claim.PostClaimOrchestrationHandler;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseEvent;
import uk.gov.hmcts.cmc.claimstore.events.claimantresponse.ClaimantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.offer.AgreementCountersignedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimCreatedEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse.PartAdmission;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

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
    private DocumentsService documentsService;

    @Mock
    private PostClaimOrchestrationHandler postClaimOrchestrationHandler;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private SupportController controller;

    private Claim sampleClaim;

    @Before
    public void setUp() {
        controller = new SupportController(claimService, userService, documentGenerator,
            moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler,
            claimantResponseStaffNotificationHandler, documentsService, postClaimOrchestrationHandler,
            false
        );
        sampleClaim = SampleClaim.getDefault();
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotResendRPANotificationsWhenRequestBodyIsEmpty() {
        controller.resendRPANotifications(AUTHORISATION, Collections.emptyList());
    }

    @Test(expected = NotFoundException.class)
    public void shouldNotResendRPANotificationsWhenRequestBodyClaimsDoesNotExistForMultipleClaims() {
        // given
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));

        // when
        controller.resendRPANotifications(AUTHORISATION, ImmutableList.of(CLAIM_REFERENCE, "000CM003"));

        // then
        verify(documentGenerator, never()).generateForCitizenRPA(any());
    }

    @Test
    public void shouldResendRPANotifications() {
        // given
        String letterHolderId = "333";
        GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", letterHolderId);
        given(userService.generatePin(anyString(), eq(AUTHORISATION))).willReturn(pinResponse);

        // when
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(USER_DETAILS);

        when(claimService.linkLetterHolder(sampleClaim, letterHolderId, AUTHORISATION)).thenReturn(sampleClaim);

        controller.resendRPANotifications(AUTHORISATION, Collections.singletonList(CLAIM_REFERENCE));

        // then
        verify(documentGenerator).generateForCitizenRPA(any());
    }

    @Test
    public void shouldResendStaffNotifications() {
        // given
        sampleClaim = SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withDefendantId(null)
            .build();

        String letterHolderId = "333";
        GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", letterHolderId);
        given(userService.generatePin(anyString(), eq(AUTHORISATION))).willReturn(pinResponse);

        // when
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(sampleClaim));
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(USER_DETAILS);

        when(claimService.linkLetterHolder(sampleClaim, letterHolderId, AUTHORISATION)).thenReturn(sampleClaim);

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claim-issued");

        // then
        verify(documentGenerator).generateForNonRepresentedClaim(any());
    }

    @Test
    public void shouldResendStaffNotificationsForIntentToProceed() {
        // given
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
            claimantResponseStaffNotificationHandler, documentsService, postClaimOrchestrationHandler,
            true
        );

        // when
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER))).thenReturn(Optional.of(sampleClaim));

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "intent-to-proceed", AUTHORISATION);

        // then
        verify(claimantResponseStaffNotificationHandler)
            .notifyStaffWithClaimantsIntentionToProceed(new ClaimantResponseEvent(sampleClaim));
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
                SampleClaimantResponse.ClaimantResponseAcceptation.builder()
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
                SampleClaimantResponse.ClaimantResponseAcceptation.builder()
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
                SampleClaimantResponse.ClaimantResponseAcceptation.builder()
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
    public void shouldUploadSealedClaimDocument() {
        Claim claim = SampleClaim.getWithSealedClaimDocument();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SEALED_CLAIM, AUTHORISATION);
        verify(documentsService).generateDocument(claim.getExternalId(), SEALED_CLAIM, AUTHORISATION);
    }

    @Test
    public void shouldUploadClaimIssueReceiptDocument() {
        Claim claim = SampleClaim.getWithClaimIssueReceiptDocument();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, CLAIM_ISSUE_RECEIPT, AUTHORISATION);
        verify(documentsService).generateDocument(claim.getExternalId(), CLAIM_ISSUE_RECEIPT, AUTHORISATION);
    }

    @Test
    public void shouldUploadDefendantResponseReceiptDocument() {
        Claim claim = SampleClaim.getWithDefendantResponseReceiptDocument();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, DEFENDANT_RESPONSE_RECEIPT, AUTHORISATION);
        verify(documentsService).generateDocument(claim.getExternalId(), DEFENDANT_RESPONSE_RECEIPT, AUTHORISATION);
    }

    @Test
    public void shouldUploadSettlementAgreementDocument() {
        Claim claim = SampleClaim.getWithSettlementAgreementDocument();
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SETTLEMENT_AGREEMENT, AUTHORISATION);
        verify(documentsService).generateDocument(claim.getExternalId(), SETTLEMENT_AGREEMENT, AUTHORISATION);
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenClaimIsNotFound() {
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.empty());
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Claim " + CLAIM_REFERENCE + " does not exist");
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SEALED_CLAIM, AUTHORISATION);
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenAuthorisationStringIsInvalid() {
        Claim claim = SampleClaim.getWithSettlement(SampleSettlement.validDefaults());
        when(claimService.getClaimByReferenceAnonymous(CLAIM_REFERENCE)).thenReturn(Optional.of(claim));
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("Authorisation is required");
        controller.uploadDocumentToDocumentManagement(CLAIM_REFERENCE, SEALED_CLAIM, "");
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

}
