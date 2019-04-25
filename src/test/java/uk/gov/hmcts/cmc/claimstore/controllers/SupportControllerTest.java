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
import uk.gov.hmcts.cmc.claimstore.events.response.DefendantResponseStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedStaffNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;

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
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;

@RunWith(MockitoJUnitRunner.class)
public class SupportControllerTest {

    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String CLAIMREFERENCENUMBER = "000CM001";
    private static final String RESPONSESUBMITTED = "response-submitted";
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
    private DocumentsService documentsService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private SupportController controller;

    private Claim sampleClaim;

    @Before
    public void setUp() {
        controller = new SupportController(claimService, userService, documentGenerator,
            moreTimeRequestedStaffNotificationHandler, defendantResponseStaffNotificationHandler,
            ccjStaffNotificationHandler, agreementCountersignedStaffNotificationHandler,
            documentsService
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
        sendList.add(CLAIMREFERENCENUMBER);
        sendList.add("000CM003");
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER))).thenReturn(Optional.of(sampleClaim));

        // when
        controller.resendRPANotifications(AUTHORISATION, sendList);

        // then
        verify(documentGenerator, never()).generateForCitizenRPA(any());
    }

    @Test
    public void shouldResendRPANotifications() {
        // given
        List<String> sendList = new ArrayList<>();
        sendList.add(CLAIMREFERENCENUMBER);
        String letterHolderId = "333";
        GeneratePinResponse pinResponse = new GeneratePinResponse("pin-123", letterHolderId);
        given(userService.generatePin(anyString(), eq(AUTHORISATION))).willReturn(pinResponse);

        // when
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER))).thenReturn(Optional.of(sampleClaim));
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(USER_DETAILS);

        when(claimService.linkLetterHolder(eq(sampleClaim.getId()), eq(letterHolderId))).thenReturn(sampleClaim);

        controller.resendRPANotifications(AUTHORISATION, sendList);

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
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER))).thenReturn(Optional.of(sampleClaim));
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(USER_DETAILS);
        when(claimService.linkLetterHolder(eq(sampleClaim.getId()), eq(letterHolderId))).thenReturn(sampleClaim);

        controller.resendStaffNotifications(sampleClaim.getReferenceNumber(), "claim-issued", AUTHORISATION);

        // then
        verify(documentGenerator).generateForNonRepresentedClaim(any());
    }

    @Test
    public void shouldThrowExceptionIfDefendantResponseSubmittedWhenNoDefendantResponse() {
        exceptionRule.expect(ConflictException.class);
        exceptionRule.expectMessage("Claim " + CLAIMREFERENCENUMBER + " does not have associated response");
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER)))
            .thenReturn(Optional.of(SampleClaim.withNoResponse()));
        controller.resendStaffNotifications(CLAIMREFERENCENUMBER, RESPONSESUBMITTED, AUTHORISATION);
    }

    @Test
    public void shouldUploadSealedClaimDocument() {
        Claim claim = SampleClaim.getWithSealedClaimDocument();
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER)))
            .thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIMREFERENCENUMBER, SEALED_CLAIM, AUTHORISATION);
        verify(documentsService).generateSealedClaim(claim.getExternalId(), AUTHORISATION);
    }

    @Test
    public void shouldUploadClaimIssueReceiptDocument() {
        Claim claim = SampleClaim.getWithClaimIssueReceiptDocument();
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER)))
            .thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIMREFERENCENUMBER, CLAIM_ISSUE_RECEIPT, AUTHORISATION);
        verify(documentsService).generateClaimIssueReceipt(claim.getExternalId(), AUTHORISATION);
    }

    @Test
    public void shouldUploadDefendantResponseReceiptDocument() {
        Claim claim = SampleClaim.getWithDefendantResponseReceiptDocument();
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER)))
            .thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIMREFERENCENUMBER, DEFENDANT_RESPONSE_RECEIPT, AUTHORISATION);
        verify(documentsService).generateDefendantResponseReceipt(claim.getExternalId(), AUTHORISATION);
    }

    @Test
    public void shouldUploadCCJDocument() {
        Claim claim = SampleClaim.getWithCCJRequestDocument();
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER)))
            .thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIMREFERENCENUMBER, CCJ_REQUEST, AUTHORISATION);
        verify(documentsService).generateCountyCourtJudgement(claim.getExternalId(), AUTHORISATION);
    }

    @Test
    public void shouldUploadSettlementAgreementDocument() {
        Claim claim = SampleClaim.getWithSettlementAgreementDocument();
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER)))
            .thenReturn(Optional.of(claim));
        controller.uploadDocumentToDocumentManagement(CLAIMREFERENCENUMBER, SETTLEMENT_AGREEMENT, AUTHORISATION);
        verify(documentsService).generateSettlementAgreement(claim.getExternalId(), AUTHORISATION);
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenClaimIsNotFound() {
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER)))
            .thenReturn(Optional.empty());
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Claim " + CLAIMREFERENCENUMBER + " does not exist");
        controller.uploadDocumentToDocumentManagement(CLAIMREFERENCENUMBER, SEALED_CLAIM, AUTHORISATION);
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenAuthorisationStringIsInvalid() {
        Claim claim = SampleClaim.getWithSettlement(SampleSettlement.validDefaults());
        when(claimService.getClaimByReferenceAnonymous(eq(CLAIMREFERENCENUMBER)))
            .thenReturn(Optional.of(claim));
        exceptionRule.expect(BadRequestException.class);
        exceptionRule.expectMessage("Authorisation is required");
        controller.uploadDocumentToDocumentManagement(CLAIMREFERENCENUMBER, SEALED_CLAIM, "");
    }

}
