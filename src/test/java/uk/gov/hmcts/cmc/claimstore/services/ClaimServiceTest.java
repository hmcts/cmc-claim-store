package uk.gov.hmcts.cmc.claimstore.services;

import org.apache.http.HttpException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.*;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimAuthorisationRule;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.rules.PaidInFullRule;
import uk.gov.hmcts.cmc.claimstore.rules.ReviewOrderRule;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.BreathingSpaceType;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.Payment;
import uk.gov.hmcts.cmc.domain.models.PaymentStatus;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.ioc.CreatePaymentResponse;
import uk.gov.hmcts.cmc.domain.models.legalrep.LegalRepUpdate;
import uk.gov.hmcts.cmc.domain.models.legalrep.PaymentReference;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SamplePayment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;
import uk.gov.hmcts.cmc.launchdarkly.LaunchDarklyClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESUME_CLAIM_PAYMENT_CITIZEN;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.UPDATE_HELP_WITH_FEE_CLAIM;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_LEGAL;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.HWF_CLAIM_CREATED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NUMBER_OF_RECONSIDERATION;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.CREATE;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.CLAIM_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.DEFENDANT_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.LETTER_HOLDER_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.NOT_REQUESTED_FOR_MORE_TIME;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.SUBMITTER_EMAIL;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.RESPONSE_DEADLINE;

@RunWith(MockitoJUnitRunner.class)
public class ClaimServiceTest {
    private static final String RETURN_URL = "http://returnUrl.test";

    private static final ClaimData VALID_APP = SampleClaimData.submittedByClaimant();
    private static final ClaimData VALID_LEGAL_APP = SampleClaimData.submittedByLegalRepresentative();
    private static final String DEFENDANT_EMAIL = "defendant@email.com";
    private static final Claim claim = createClaimModel(VALID_APP, LETTER_HOLDER_ID);
    private static final Claim representedClaim = createRepresentedClaimModel(VALID_LEGAL_APP);
    private static final String AUTHORISATION = "Bearer: aaa";

    private static final UserDetails UNAUTHORISED_USER_DETAILS = SampleUserDetails.builder().withUserId("300").build();
    private static final UserDetails VALID_DEFENDANT
        = SampleUserDetails.builder().withUserId(DEFENDANT_ID).withMail(DEFENDANT_EMAIL).build();

    private static final UserDetails VALID_CLAIMANT
        = SampleUserDetails.builder().withUserId(USER_ID).withMail(SUBMITTER_EMAIL).build();

    private static final User UNAUTHORISED_USER = new User(AUTHORISATION, UNAUTHORISED_USER_DETAILS);
    private static final User USER = new User(AUTHORISATION, VALID_CLAIMANT);

    private ClaimService claimService;

    @Mock
    private CaseRepository caseRepository;
    @Mock
    private UserService userService;
    @Mock
    private IssueDateCalculator issueDateCalculator;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private EventProducer eventProducer;
    @Mock
    private AppInsights appInsights;
    @Mock
    LaunchDarklyClient launchDarklyClient;

    @Captor
    private ArgumentCaptor<Claim> claimArgumentCaptor;

    @Before
    public void setup() {
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(VALID_DEFENDANT);
        when(userService.getUser(eq(AUTHORISATION))).thenReturn(USER);

        claimService = new ClaimService(
            caseRepository,
            userService,
            issueDateCalculator,
            responseDeadlineCalculator,
            new MoreTimeRequestRule(new ClaimDeadlineService()),
            eventProducer,
            appInsights,
            new PaidInFullRule(),
            new ClaimAuthorisationRule(userService),
            new ReviewOrderRule(),
            launchDarklyClient,
            true);
    }

    @Test
    public void getClaimByLetterHolderIdShouldCallRepositoryWhenValidClaimIsReturned() {

        Claim claim = createClaimModel(VALID_APP, LETTER_HOLDER_ID);
        Optional<Claim> result = Optional.of(claim);

        when(caseRepository.getByLetterHolderId(eq(LETTER_HOLDER_ID), any())).thenReturn(result);
        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(SampleUserDetails.builder().withUserId(LETTER_HOLDER_ID).build());

        Claim claimApplication = claimService.getClaimByLetterHolderId(LETTER_HOLDER_ID, AUTHORISATION);
        assertThat(claimApplication).isEqualTo(claim);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByLetterHolderIdShouldThrowExceptionWhenClaimDoesNotExist() {

        Optional<Claim> result = empty();
        String letterHolderId = "0";

        when(caseRepository.getByLetterHolderId(eq(letterHolderId), any())).thenReturn(result);

        claimService.getClaimByLetterHolderId(letterHolderId, AUTHORISATION);
    }

    @Test(expected = NotFoundException.class)
    public void getClaimByExternalIdShouldThrowExceptionWhenClaimDoesNotExist() {

        Optional<Claim> result = empty();
        String externalId = "does not exist";

        when(caseRepository.getClaimByExternalId(eq(externalId), eq(USER))).thenReturn(result);

        claimService.getClaimByExternalId(externalId, USER);
    }

    @Test
    public void getFilteredClaimByExternalIdShouldFilterDocuments() {

        Claim claimWithDocs = SampleClaim.builder()
            .withOrderDocument(URI.create("http://localhost/doc.pdf"))
            .withClaimIssueReceiptDocument(URI.create("http://localhost/doc.pdf"))
            .withSealedClaimDocument(URI.create("http://localhost/doc.pdf"))
            .withSettlementAgreementDocument(URI.create("http://localhost/doc.pdf"))
            .build();
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(claimWithDocs));

        List<ClaimDocument> docList = claimService.getFilteredClaimByExternalId(EXTERNAL_ID, AUTHORISATION)
            .getClaimDocumentCollection().map(ClaimDocumentCollection::getClaimDocuments)
            .orElse(Collections.emptyList());

        assertAll(
            () -> assertTrue(docList
                .stream().map(ClaimDocument::getDocumentType)
                .anyMatch(ClaimDocumentType.CLAIM_ISSUE_RECEIPT::equals)),
            () -> assertFalse(docList
                .stream().map(ClaimDocument::getDocumentType)
                .anyMatch(ClaimDocumentType.SEALED_CLAIM::equals))
        );
    }

    @Test
    public void saveClaimShouldFinishSuccessfully() {
        //given
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(ISSUE_DATE);
        when(responseDeadlineCalculator.calculateResponseDeadline(eq(ISSUE_DATE))).thenReturn(RESPONSE_DEADLINE);
        when(caseRepository.saveClaim(eq(USER), any())).thenReturn(claim);

        claimService = new ClaimService(
            caseRepository,
            userService,
            issueDateCalculator,
            responseDeadlineCalculator,
            new MoreTimeRequestRule(new ClaimDeadlineService()),
            eventProducer,
            appInsights,
            new PaidInFullRule(),
            new ClaimAuthorisationRule(userService),
            new ReviewOrderRule(),
            launchDarklyClient,
            true);

        ClaimData claimData = SampleClaimData.validDefaults();

        //when
        Claim createdClaim = claimService
            .saveClaim(USER_ID, claimData, AUTHORISATION, singletonList(ADMISSIONS.getValue()));

        //verify
        ClaimData outputClaimData = claim.getClaimData();
        assertThat(createdClaim.getClaimData()).isEqualTo(outputClaimData);

        verify(userService, never()).generatePin(eq(outputClaimData.getDefendant().getName()), eq(AUTHORISATION));
        verify(caseRepository, once()).saveClaim(any(User.class), any(Claim.class));
        verify(eventProducer, once()).createClaimCreatedEvent(eq(createdClaim), anyString(), eq(AUTHORISATION));
    }

    @Test
    public void saveClaimShouldThrowExceptionWhenClaimCreateFeatureDisabled() {
        claimService = new ClaimService(
            caseRepository,
            userService,
            issueDateCalculator,
            responseDeadlineCalculator,
            new MoreTimeRequestRule(new ClaimDeadlineService()),
            eventProducer,
            appInsights,
            new PaidInFullRule(),
            new ClaimAuthorisationRule(userService),
            new ReviewOrderRule(),
            launchDarklyClient,
            false);

        ClaimData claimData = SampleClaimData.validDefaults();

        Assert.assertThrows(ClaimCreationDisabledException.class, () -> claimService
            .saveClaim(USER_ID, claimData, AUTHORISATION, singletonList(ADMISSIONS.getValue())));

    }

    @Test
    public void saveLegalRepClaimShouldFinishSuccessfully() {
        ClaimData claimData = SampleClaimData.submittedByLegalRepresentative();

        when(caseRepository.saveRepresentedClaim(eq(USER), any(Claim.class)))
            .thenReturn(representedClaim);

        Claim createdLegalRepClaim = claimService
            .saveRepresentedClaim(
                USER_ID, claimData, AUTHORISATION);

        assertThat(createdLegalRepClaim.getClaimData()).isEqualTo(representedClaim.getClaimData());

        verify(caseRepository, once()).saveRepresentedClaim(
            eq(USER), claimArgumentCaptor.capture());

        Claim argumentCaptorValue = claimArgumentCaptor.getValue();
        assertThat(argumentCaptorValue.getClaimData()).isEqualTo(claimData);
        assertThat(argumentCaptorValue.getExternalId()).isEqualTo(claimData.getExternalId().toString());
        assertThat(argumentCaptorValue.getSubmitterId()).isEqualTo(USER_ID);
        assertThat(argumentCaptorValue.getSubmitterEmail()).isEqualTo(USER.getUserDetails().getEmail());
    }

    @Test
    public void requestMoreTimeToRespondShouldFinishSuccessfully() {

        LocalDate newDeadline = RESPONSE_DEADLINE.plusDays(20);

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(claim));
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any()))
            .thenReturn(newDeadline);

        claimService.requestMoreTimeForResponse(EXTERNAL_ID, AUTHORISATION);

        verify(caseRepository, once()).requestMoreTimeForResponse(eq(AUTHORISATION), eq(claim), eq(newDeadline));
    }

    @Test(expected = NotFoundException.class)
    public void requestMoreTimeToRespondShouldThrowNotFoundExceptionWhenClaimNotFound() {
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any())).thenReturn(empty());

        claimService.requestMoreTimeForResponse(EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = MoreTimeAlreadyRequestedException.class)
    public void requestMoreTimeForResponseThrowsMoreTimeAlreadyRequestedExceptionWhenMoreTimeRequestForSecondTime() {

        Claim claim = createClaimModel(RESPONSE_DEADLINE, true);

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(claim));

        claimService.requestMoreTimeForResponse(EXTERNAL_ID, AUTHORISATION);
    }

    @Test
    public void getClaimByClaimantEmailShouldCallCaseRepository() {
        when(caseRepository.getByClaimantEmail(eq(claim.getSubmitterEmail()), anyString()))
            .thenReturn(singletonList(claim));

        List<Claim> claims = claimService.getClaimByClaimantEmail(claim.getSubmitterEmail(), AUTHORISATION);

        assertThat(claims).containsExactly(claim);
    }

    @Test
    public void getClaimByDefendantEmailShouldCallCaseRepository() {
        when(caseRepository.getByDefendantEmail(eq(claim.getDefendantEmail()), anyString()))
            .thenReturn(singletonList(claim));

        List<Claim> claims = claimService.getClaimByDefendantEmail(claim.getDefendantEmail(), AUTHORISATION);

        assertThat(claims).containsExactly(claim);
    }

    @Test
    public void getClaimsByStateShouldCallCaseRepository() {
        when(caseRepository.getClaimsByState(eq(CREATE), any()))
            .thenReturn(singletonList(claim));

        List<Claim> claims = claimService.getClaimsByState(CREATE, USER);

        assertThat(claims).containsExactly(claim);
    }

    @Test
    public void paidInFullShouldFinishSuccessfully() {

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(VALID_CLAIMANT);
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(claim));
        PaidInFull paidInFull = new PaidInFull(now());

        claimService.paidInFull(EXTERNAL_ID, paidInFull, AUTHORISATION);

        verify(caseRepository, once()).paidInFull(eq(claim), eq(paidInFull), eq(AUTHORISATION));

        verify(eventProducer, once()).createPaidInFullEvent(eq(claim));

        verify(appInsights).trackEvent(AppInsightsEvent.PAID_IN_FULL,
            AppInsights.REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    @Test(expected = ConflictException.class)
    public void paidInFullShouldThrowConflictExceptionIfAlreadyPaidInFull() {

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(VALID_CLAIMANT);

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(createPaidInFullClaim(now())));

        claimService.paidInFull(EXTERNAL_ID, new PaidInFull(now()), AUTHORISATION);
    }

    @Test(expected = NotFoundException.class)
    public void paidInFullShouldThrowNotFoundExceptionWhenClaimNotFound() {
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any())).thenReturn(empty());

        claimService.paidInFull(EXTERNAL_ID, new PaidInFull(now()), AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void getBySubmitterIdShouldThrowExceptionWhenCallerNotAuthorised() {
        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(SampleUserDetails.builder().withUserId("300").build());

        claimService.getClaimBySubmitterId(USER_ID, AUTHORISATION, 1);
    }

    @Test
    public void getBySubmitterIdWhenPageNumberIsNull() {
        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(SampleUserDetails.builder().withUserId("1").build());

        List<Claim> result = claimService.getClaimBySubmitterId(USER_ID, AUTHORISATION, null);

        assertThat(result).isNotNull();
    }

    @Test
    public void getBySubmitterIdWhenPageNumberIsNotNull() {
        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(SampleUserDetails.builder().withUserId("1").build());

        List<Claim> result = claimService.getClaimBySubmitterId(USER_ID, AUTHORISATION, 1);

        assertThat(result).isNotNull();
    }

    @Test(expected = ForbiddenActionException.class)
    public void getByLetterHolderIdShouldThrowExceptionWhenCallerNotAuthorised() {
        when(caseRepository
            .getByLetterHolderId(claim.getLetterHolderId(), AUTHORISATION))
            .thenReturn(Optional.of(claim));
        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(SampleUserDetails.builder().withUserId("300").build());

        claimService.getClaimByLetterHolderId(LETTER_HOLDER_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void getByExternalIdShouldThrowExceptionWhenCallerNotAuthorised() {
        when(caseRepository.getClaimByExternalId(claim.getExternalId(), UNAUTHORISED_USER))
            .thenReturn(Optional.of(claim));

        claimService.getClaimByExternalId(claim.getExternalId(), UNAUTHORISED_USER);
    }

    @Test(expected = ForbiddenActionException.class)
    public void getByReferenceShouldThrowExceptionWhenCallerNotAuthorised() {
        when(caseRepository.getByClaimReferenceNumber(claim.getReferenceNumber(), AUTHORISATION))
            .thenReturn(Optional.of(claim));

        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(SampleUserDetails.builder().withUserId("300").build());

        claimService.getClaimByReference(claim.getReferenceNumber(), AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void requestMoreTimeShouldThrowExceptionWhenCallerNotAuthorised() {
        when(caseRepository.getClaimByExternalId(claim.getExternalId(), UNAUTHORISED_USER))
            .thenReturn(Optional.of(claim));
        when(userService.getUser(eq(AUTHORISATION))).thenReturn(UNAUTHORISED_USER);

        claimService.requestMoreTimeForResponse(claim.getExternalId(), AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void getByDefendantIdShouldThrowExceptionWhenCallerNotAuthorised() {
        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(UNAUTHORISED_USER_DETAILS);

        claimService.getClaimByDefendantId(USER_ID, AUTHORISATION, 1);
    }

    @Test(expected = ForbiddenActionException.class)
    public void paidInFullShouldThrowExceptionWhenCallerNotAuthorised() {
        when(caseRepository.getClaimByExternalId(claim.getExternalId(), USER))
            .thenReturn(Optional.of(claim));

        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(SampleUserDetails.builder().withUserId("300").build());

        claimService.paidInFull(claim.getExternalId(), new PaidInFull(now()), AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveReDeterminationShouldThrowExceptionWhenCallerNotAuthorised() {
        when(userService.getUserDetails(AUTHORISATION))
            .thenReturn(SampleUserDetails.builder().withUserId("300").build());

        claimService.saveReDetermination(AUTHORISATION, claim, new ReDetermination("", MadeBy.CLAIMANT));
    }

    @Test
    public void saveDefendantResponseShouldCalculateClaimantResponseDeadline() {
        LocalDate deadline = now().plusDays(99);
        when(responseDeadlineCalculator.calculateClaimantResponseDeadline(any(LocalDate.class)))
            .thenReturn(deadline);
        claimService.saveDefendantResponse(claim, DEFENDANT_EMAIL, SampleResponse.validDefaults(), AUTHORISATION);
        verify(caseRepository).saveDefendantResponse(
            any(Claim.class),
            eq(DEFENDANT_EMAIL),
            any(Response.class),
            eq(deadline),
            eq(AUTHORISATION)
        );
    }

    @Test
    public void updateStateShouldCallCaseRepository() {
        claimService.updateClaimState(AUTHORISATION, claim, ClaimState.OPEN);

        verify(caseRepository).updateClaimState(
            eq(AUTHORISATION),
            eq(claim.getId()),
            eq(ClaimState.OPEN)
        );
    }

    @Test
    public void addBulkPrintDetailsToClaimShouldCallCaseRepository() {
        String letterId = UUID.randomUUID().toString();
        List<BulkPrintDetails> printCollection
            = List.of(BulkPrintDetails.builder().printRequestId(letterId).build());
        claimService.addBulkPrintDetails(
            AUTHORISATION, printCollection, CaseEvent.ADD_BULK_PRINT_DETAILS, claim);

        verify(caseRepository).addBulkPrintDetailsToClaim(
            eq(AUTHORISATION),
            eq(printCollection),
            eq(CaseEvent.ADD_BULK_PRINT_DETAILS),
            eq(claim)
        );
    }

    @Test
    public void updateClaimSubmissionOperationIndicatorsShouldCallCaseRepository() {
        ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators = ClaimSubmissionOperationIndicators
            .builder().build();
        claimService.updateClaimSubmissionOperationIndicators(AUTHORISATION, claim, claimSubmissionOperationIndicators);
        verify(caseRepository).updateClaimSubmissionOperationStatus(
            eq(AUTHORISATION),
            eq(claim.getId()),
            eq(claimSubmissionOperationIndicators),
            eq(RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS)
        );
    }

    @Test
    public void initiatePaymentShouldFinishSuccessfully() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withPayment(
                    Payment.builder().nextUrl("http://nexturl.test").build())
                .build())
            .build();

        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(ISSUE_DATE);
        when(responseDeadlineCalculator.calculateResponseDeadline(eq(ISSUE_DATE))).thenReturn(RESPONSE_DEADLINE);
        when(caseRepository.initiatePayment(eq(USER), any(Claim.class)))
            .thenReturn(claim);

        CreatePaymentResponse response = claimService.initiatePayment(AUTHORISATION, VALID_APP);

        CreatePaymentResponse expectedResponse = CreatePaymentResponse.builder()
            .nextUrl("http://nexturl.test")
            .build();
        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    public void resumePaymentShouldReturnReturnUrlIfPaymentIsSuccessful() {

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(claim));
        when(caseRepository.saveCaseEventIOC(USER, claim, RESUME_CLAIM_PAYMENT_CITIZEN))
            .thenReturn(claim);
        CreatePaymentResponse response = claimService.resumePayment(AUTHORISATION, claimData);

        assertThat(response.getNextUrl()).isEqualTo(format(RETURN_URL), claim.getExternalId());
    }

    @Test
    public void resumePaymentShouldReturnNextUrlIfPaymentIsNotSuccessful() {

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            .withPayment(
                SamplePayment.builder()
                    .status(PaymentStatus.INITIATED)
                    .nextUrl("http://payment.nexturl.test")
                    .build()
            )
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(claim));
        when(caseRepository.saveCaseEventIOC(USER, claim, RESUME_CLAIM_PAYMENT_CITIZEN))
            .thenReturn(claim);
        CreatePaymentResponse response = claimService.resumePayment(AUTHORISATION, claimData);

        assertThat(response.getNextUrl()).isEqualTo("http://payment.nexturl.test");
    }

    @Test
    public void resumePaymentShouldUpdateClaimDataBeingPassed() {

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            .withFeeAccountNumber("OLD_ACCOUNT")
            .withAmount(SampleAmountBreakdown.builder().build())
            .withPayment(
                SamplePayment.builder()
                    .status(PaymentStatus.INITIATED)
                    .nextUrl("http://payment.nexturl.test")
                    .build()
            )
            .build();

        ClaimData claimDataToBeUpdated = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            .withFeeAccountNumber("NEW_ACCOUNT")
            .withAmount(SampleAmountBreakdown.withThousandAsAmount().build())
            .withPayment(
                SamplePayment.builder()
                    .status(PaymentStatus.INITIATED)
                    .nextUrl("http://payment.nexturl.test")
                    .build()
            )
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(claim));
        when(caseRepository.saveCaseEventIOC(eq(USER), any(), eq(RESUME_CLAIM_PAYMENT_CITIZEN)))
            .thenReturn(claim);

        claimService.resumePayment(AUTHORISATION, claimDataToBeUpdated);

        verify(caseRepository, once()).saveCaseEventIOC(any(), claimArgumentCaptor.capture(), any());

        Claim argumentCaptorValue = claimArgumentCaptor.getValue();

        assertThat(argumentCaptorValue.getClaimData().getFeeAccountNumber().orElse("")).isEqualTo("NEW_ACCOUNT");
        AmountBreakDown finalAmount = (AmountBreakDown) argumentCaptorValue.getClaimData().getAmount();
        assertThat(finalAmount.getTotalAmount()).isEqualTo(new BigDecimal("1000.99"));
        assertThat(argumentCaptorValue.getClaimData()).isEqualTo(claimDataToBeUpdated);
    }

    @Test
    public void saveCitizenClaimShouldFinishSuccessfully() {

        when(caseRepository.getClaimByExternalId(VALID_APP.getExternalId().toString(), USER))
            .thenReturn(Optional.of(claim));
        when(caseRepository
            .saveCaseEventIOC(eq(USER), any(Claim.class), eq(CREATE_CITIZEN_CLAIM)))
            .thenReturn(claim);

        Claim createdClaim = claimService.createCitizenClaim(
            AUTHORISATION,
            VALID_APP,
            singletonList(ADMISSIONS.getValue()));

        assertThat(createdClaim).isEqualTo(claim);
    }

    @Test
    public void saveReviewOrderShouldFinishSuccessfully() {

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(VALID_CLAIMANT);
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(claim));

        ReviewOrder reviewOrder = SampleReviewOrder.getDefault();

        claimService.saveReviewOrder(EXTERNAL_ID, reviewOrder, AUTHORISATION);

        verify(caseRepository).saveReviewOrder(eq(claim.getId()), eq(reviewOrder), eq(AUTHORISATION));

        verify(appInsights).trackEvent(eq(NUMBER_OF_RECONSIDERATION),
            eq(AppInsights.REFERENCE_NUMBER),
            eq(claim.getReferenceNumber()));
    }

    @Test(expected = ConflictException.class)
    public void saveReviewOrderShouldThrowConflictExceptionIfAlreadyExists() {
        ReviewOrder reviewOrder = SampleReviewOrder.getDefault();

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(VALID_CLAIMANT);

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(createSampleClaim().withReviewOrder(reviewOrder).build()));

        claimService.saveReviewOrder(EXTERNAL_ID, reviewOrder, AUTHORISATION);
    }

    @Test(expected = NotFoundException.class)
    public void saveReviewOrderShouldThrowNotFoundExceptionWhenClaimNotFound() {
        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any())).thenReturn(empty());

        claimService.saveReviewOrder(EXTERNAL_ID, SampleReviewOrder.getDefault(), AUTHORISATION);
    }

    @Test
    public void testSaveHelpWithFeesClaim() {
        UUID externalId = UUID.randomUUID();
        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(externalId)
            .withHelpWithFeesNumber("HWF01234")
            .withHelpWithFeesType("Claim Issue").build();
        when(issueDateCalculator.calculateIssueDay(any())).thenReturn(LocalDate.now());
        claimService.saveHelpWithFeesClaim(USER_ID, claimData, AUTHORISATION, singletonList(ADMISSIONS.getValue()));

        verify(caseRepository).saveHelpWithFeesClaim(any(), any());
        verify(appInsights).trackEvent(HWF_CLAIM_CREATED, AppInsights.CLAIM_EXTERNAL_ID, externalId.toString());
    }

    @Test
    public void testUpdateHelpWithFeesClaim() {

        when(caseRepository.getClaimByExternalId(VALID_APP.getExternalId().toString(), USER))
            .thenReturn(Optional.of(claim));
        when(caseRepository
            .updateHelpWithFeesClaim(eq(USER), any(Claim.class), eq(UPDATE_HELP_WITH_FEE_CLAIM)))
            .thenReturn(claim);

        claimService.updateHelpWithFeesClaim(AUTHORISATION,
            VALID_APP.toBuilder()
                .helpWithFeesType("Claim Issue")
                .helpWithFeesNumber("HWF12345").build(), singletonList(ADMISSIONS.getValue()));

        verify(caseRepository).updateHelpWithFeesClaim(any(), any(), eq(UPDATE_HELP_WITH_FEE_CLAIM));
        verify(appInsights).trackEvent(HWF_CLAIM_CREATED, AppInsights.CLAIM_EXTERNAL_ID,
            VALID_APP.getExternalId().toString());
    }

    @Test(expected = ConflictException.class)
    public void saveHelpWithFeesClaimShouldThrowConflictExceptionIfAlreadyExists() {
        ClaimData claimData = SampleClaimData.builder()
            .withHelpWithFeesNumber("HWF01234")
            .withHelpWithFeesType("ClaimIssue").build();

        when(caseRepository.getClaimByExternalId(eq(EXTERNAL_ID), any()))
            .thenReturn(Optional.of(mock(Claim.class)));

        claimService.saveHelpWithFeesClaim(USER_ID, claimData, AUTHORISATION, singletonList(ADMISSIONS.getValue()));

        verifyNoInteractions(appInsights);
    }

    @Test
    public void testSaveBreathingSpaceDetails() throws HttpException {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now(), "NO");

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            // .withBreathingSpace(breathingSpace)
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(AppInsights.CLAIM_EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));
        when(caseRepository
            .saveBreathingSpaceDetails(any(Claim.class), any(BreathingSpace.class), anyString()))
            .thenReturn(claim);

        claimService.saveBreathingSpaceDetails(AppInsights.CLAIM_EXTERNAL_ID, breathingSpace,
            AUTHORISATION);

        verify(caseRepository).saveBreathingSpaceDetails(any(Claim.class), any(BreathingSpace.class), anyString());
    }

    @Test
    public void testSaveBreathingSpaceDetailsWithClaimStateTransferred() throws HttpException {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now(), "NO");

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            // .withBreathingSpace(breathingSpace)
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .withState(ClaimState.TRANSFERRED)
            .build();

        when(caseRepository.getClaimByExternalId(AppInsights.CLAIM_EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));
        try {
            claimService.saveBreathingSpaceDetails(AppInsights.CLAIM_EXTERNAL_ID, breathingSpace,
                AUTHORISATION);
        } catch (HttpException e) {
            assert (e.getMessage().contains("This Event cannot be triggered since the claim "
                + "is no longer part of the online civil money claims journey"));
        }
        assertThat(claim).isNotNull();
    }

    @Test
    public void testSaveBreathingSpaceDetailsExpectedEndDateLesserThanTodaysDate() throws HttpException {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now().minusDays(3), "NO");

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            // .withBreathingSpace(breathingSpace)
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(AppInsights.CLAIM_EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));
        try {
            claimService.saveBreathingSpaceDetails(AppInsights.CLAIM_EXTERNAL_ID, breathingSpace,
                AUTHORISATION);
        } catch (HttpException e) {
            assert (e.getMessage().contains("The expected end date must not be before today's date"));
        }
        assertThat(claim).isNotNull();
    }

    @Test
    public void testSaveBreathingSpaceDetailsWithBsEnteredInsolvencyDateIsMoreThanCurrentDate() throws HttpException {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now().plusDays(3), null, LocalDate.now(), "NO");

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            // .withBreathingSpace(breathingSpace)
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(AppInsights.CLAIM_EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        try {
            claimService.saveBreathingSpaceDetails(AppInsights.CLAIM_EXTERNAL_ID, breathingSpace,
                AUTHORISATION);
        } catch (HttpException e) {
            assert (e.getMessage().contains("The start date must not be after today's date"));
        }
        assertThat(claim).isNotNull();
    }

    @Test
    public void testSaveBreathingSpaceDetailsWithBsReferenceNumberInvalidLength() throws HttpException {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212424323232323232",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now(), "NO");

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            // .withBreathingSpace(breathingSpace)
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(AppInsights.CLAIM_EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        try {
            claimService.saveBreathingSpaceDetails(AppInsights.CLAIM_EXTERNAL_ID, breathingSpace,
                AUTHORISATION);
        } catch (HttpException e) {
            assert (e.getMessage().contains("The reference number must be maximum of 16 Characters"));
        }
        assertThat(claim).isNotNull();
    }

    @Test
    public void testSaveBreathingSpaceDetailsForBreathingSpaceAlreadyPresent() throws HttpException {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now(), "NO");

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            .withBreathingSpace(breathingSpace)
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(AppInsights.CLAIM_EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));

        try {
            claimService.saveBreathingSpaceDetails(AppInsights.CLAIM_EXTERNAL_ID, breathingSpace,
                AUTHORISATION);
        } catch (HttpException e) {
            assert (e.getMessage().contains("Breathing Space is already entered for this Claim"));
        }
        assertThat(claim).isNotNull();
    }

    @Test
    public void testSaveBreathingSpaceDetailsWithClaimStateAsTransferred() throws HttpException {
        BreathingSpace breathingSpace = new BreathingSpace("RFFFD434343",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now(), "NO");

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            // .withBreathingSpace(breathingSpace)
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .withState(ClaimState.TRANSFERRED)
            .build();

        when(caseRepository.getClaimByExternalId(AppInsights.CLAIM_EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));
        try {
            claimService.saveBreathingSpaceDetails(AppInsights.CLAIM_EXTERNAL_ID, breathingSpace,
                AUTHORISATION);
        } catch (HttpException e) {
            assert (e.getMessage().contains("This Event cannot be triggered since the claim is no longer"
                + " part of the online civil money claims journey"));
        }

        assertThat(claim).isNotNull();
    }

    //One test case for breathing space lifted
    @Test
    public void testSaveBreathingSpaceDetailsBreathingSpaceLifted() throws HttpException {
        BreathingSpace breathingSpace = new BreathingSpace("REF12121212",
            BreathingSpaceType.STANDARD_BS_ENTERED, LocalDate.now(),
            null, LocalDate.now(), null, LocalDate.now(), "YES");

        ClaimData claimData = SampleClaimData.builder()
            .withExternalId(UUID.fromString(EXTERNAL_ID))
            // .withBreathingSpace(breathingSpace)
            .build();
        Claim claim = SampleClaim.builder()
            .withExternalId(EXTERNAL_ID)
            .withClaimData(claimData)
            .build();

        when(caseRepository.getClaimByExternalId(AppInsights.CLAIM_EXTERNAL_ID, USER))
            .thenReturn(Optional.of(claim));
        when(caseRepository
            .saveBreathingSpaceDetails(any(Claim.class), any(BreathingSpace.class), anyString()))
            .thenReturn(claim);

        claimService.saveBreathingSpaceDetails(AppInsights.CLAIM_EXTERNAL_ID, breathingSpace,
            AUTHORISATION);

        verify(caseRepository).saveBreathingSpaceDetails(any(Claim.class), any(BreathingSpace.class), anyString());
    }

    @Test
    public void shouldupdatePreferredCourtByClaimReference() {

        Claim claim = createClaimModel(VALID_APP, LETTER_HOLDER_ID);
        when(claimService.getClaimByReferenceAnonymous(
            claim.getReferenceNumber())).thenReturn(Optional.ofNullable(claim));

        claimService.updatePreferredCourtByClaimReference(claim.getReferenceNumber());

        verify(caseRepository).updatePreferredCourtByClaimReference(claim);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowExceptionWhenNoClaimRefFoundForupdatePreferredCourtByClaimReference() {

        Optional<Claim> result = empty();
        String claimNumber = "0";
        when(claimService.getClaimByReferenceAnonymous(
            claim.getReferenceNumber())).thenReturn(result);

        claimService.updatePreferredCourtByClaimReference(claimNumber);
    }

    //Update Legal rep claim
    @Test
    public void testUpdateRepresentedClaim() {

        String submitterId = "62";
        String externalId = "test-external-id";
        String pba = "PBA_NO";
        PaymentReference paymentReference = new PaymentReference("REF", "Success",
            200, null, "2021-01-01");
        LegalRepUpdate legalRepUpdate = new LegalRepUpdate(externalId,
            "1023467890123456L", new BigInteger(String.valueOf(2000)),
            "X0012", paymentReference, pba);
        when(userService.getUser(AUTHORISATION)).thenReturn(USER);
        when(caseRepository.getClaimByExternalId(legalRepUpdate.getExternalId(), USER))
            .thenReturn(Optional.of(representedClaim));
        when(caseRepository
            .updateRepresentedClaim(submitterId, USER, representedClaim, legalRepUpdate))
            .thenReturn(representedClaim);

        claimService.updateRepresentedClaim(submitterId, legalRepUpdate, AUTHORISATION);
        String referenceNumber = "referenceNumber";
        verify(caseRepository).updateRepresentedClaim(submitterId, USER, representedClaim, legalRepUpdate);
        verify(appInsights).trackEvent(CLAIM_ISSUED_LEGAL, referenceNumber, representedClaim.getReferenceNumber());
    }

    private static Claim createRepresentedClaimModel(ClaimData claimData) {
        return createSampleClaim()
            .withClaimData(claimData)
            .build();
    }

    private static Claim createClaimModel(ClaimData claimData, String letterHolderId) {
        return createSampleClaim()
            .withClaimData(claimData)
            .withLetterHolderId(letterHolderId)
            .withPreferredDQCourt("Central London County Court")
            .build();
    }

    private static Claim createClaimModel(LocalDate responseDeadline, boolean moreTimeAlreadyRequested) {
        return SampleClaim.builder()
            .withClaimId(CLAIM_ID)
            .withSubmitterId(USER_ID)
            .withLetterHolderId(LETTER_HOLDER_ID)
            .withDefendantId(DEFENDANT_ID)
            .withExternalId(EXTERNAL_ID)
            .withReferenceNumber(SampleClaim.REFERENCE_NUMBER)
            .withClaimData(VALID_APP)
            .withCreatedAt(NOW_IN_LOCAL_ZONE)
            .withIssuedOn(ISSUE_DATE)
            .withSubmitterEmail(SUBMITTER_EMAIL)
            .withResponseDeadline(responseDeadline)
            .withMoreTimeRequested(moreTimeAlreadyRequested)
            .build();
    }

    private static Claim createPaidInFullClaim(LocalDate moneyReceivedOn) {
        return createSampleClaim()
            .withClaimData(VALID_APP)
            .withMoneyReceivedOn(moneyReceivedOn)
            .build();
    }

    private static SampleClaim createSampleClaim() {
        return SampleClaim.builder()
            .withClaimId(CLAIM_ID)
            .withSubmitterId(USER_ID)
            .withDefendantId(DEFENDANT_ID)
            .withExternalId(EXTERNAL_ID)
            .withReferenceNumber(SampleClaim.REFERENCE_NUMBER)
            .withCreatedAt(NOW_IN_LOCAL_ZONE)
            .withIssuedOn(ISSUE_DATE)
            .withResponseDeadline(RESPONSE_DEADLINE)
            .withMoreTimeRequested(NOT_REQUESTED_FOR_MORE_TIME)
            .withSubmitterEmail(SUBMITTER_EMAIL);
    }
}
