package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.documents.CCJByAdmissionOrDeterminationPdfService;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.rules.CountyCourtJudgmentRule;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleOffer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LIFT_STAY;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildClaimIssueReceiptFileBaseName;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType.DEFAULT;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@RunWith(MockitoJUnitRunner.class)
public class CountyCourtJudgmentServiceTest {

    private static final String AUTHORISATION = "Bearer: aaa";

    private CountyCourtJudgmentService countyCourtJudgmentService;

    @Mock
    private ClaimService claimService;
    @Mock
    private UserService userService;

    @Mock
    private EventProducer eventProducer;
    @Mock
    private AppInsights appInsights;
    @Mock
    private CaseRepository caseRepository;
    @Mock
    private CCJByAdmissionOrDeterminationPdfService ccjByAdmissionOrDeterminationPdfService;
    @Mock
    private DocumentsService documentService;
    private static final byte[] PDF_CONTENT = {1, 2, 3, 4};
    private PDF pdf;

    private final ReDetermination reDetermination = ReDetermination.builder()
        .explanation("I feel defendant can pay")
        .partyType(MadeBy.CLAIMANT)
        .build();

    private final UserDetails userDetails = SampleUserDetails.builder().withUserId(USER_ID).build();

    @Before
    public void setup() {

        countyCourtJudgmentService = new CountyCourtJudgmentService(
            claimService,
            new AuthorisationService(),
            eventProducer,
            new CountyCourtJudgmentRule(new ClaimDeadlineService()),
            userService,
            appInsights,
            caseRepository,
            ccjByAdmissionOrDeterminationPdfService,
            documentService,
            true);

        when(userService.getUserDetails(AUTHORISATION)).thenReturn(userDetails);
    }

    @Test
    public void saveCCJByDefaultRequestSuccessfully() {

        Claim claim = SampleClaim
            .builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(ccjByDefault, EXTERNAL_ID, AUTHORISATION);

        InOrder inOrder = inOrder(claimService, eventProducer, appInsights);

        inOrder.verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any());
        inOrder.verify(eventProducer, once()).createCountyCourtJudgmentEvent(any(Claim.class), any());
        inOrder.verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_REQUESTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void saveCCJByDefaultWhenClaimIsStayed() {

        Claim claim = SampleClaim
            .builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .withResponse(SampleResponse.FullAdmission.builder().buildWithPaymentOptionImmediately())
            .withState(ClaimState.STAYED)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(caseRepository.saveCaseEvent(eq(AUTHORISATION), eq(claim), eq(LIFT_STAY))).thenReturn(claim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(ccjByDefault, EXTERNAL_ID, AUTHORISATION);

        InOrder inOrder = inOrder(claimService, eventProducer, appInsights);

        inOrder.verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any());
        inOrder.verify(eventProducer, once()).createCountyCourtJudgmentEvent(any(Claim.class), any());
        inOrder.verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_REQUESTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void saveCCJByAdmissionRequestSuccessfully() {

        Claim claim = SampleClaim
            .builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        pdf = new PDF(
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            CLAIM_ISSUE_RECEIPT
        );
        when(ccjByAdmissionOrDeterminationPdfService.createPdf(claim)).thenReturn(pdf);
        when(documentService.uploadToDocumentManagement(any(PDF.class),
            anyString(), any(Claim.class))).thenReturn(claim);
        CountyCourtJudgment ccjByAdmission = SampleCountyCourtJudgment.builder().ccjType(ADMISSIONS).build();
        countyCourtJudgmentService.save(ccjByAdmission, EXTERNAL_ID, AUTHORISATION);

        InOrder inOrder = inOrder(claimService, eventProducer, appInsights);

        inOrder.verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any());
        inOrder.verify(eventProducer, once()).createCountyCourtJudgmentEvent(any(Claim.class), any());
        inOrder.verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_REQUESTED_BY_ADMISSION),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void saveCCJAfterBreachOfSettlementAgreement() {

        Settlement settlement = new Settlement();
        settlement.makeOffer(SampleOffer.builderWithSetByDateInPast().build(), MadeBy.DEFENDANT, null);
        settlement.accept(MadeBy.CLAIMANT, null);

        Claim claim = SampleClaim
            .builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .withSettlement(settlement)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        pdf = new PDF(
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            CLAIM_ISSUE_RECEIPT
        );
        when(ccjByAdmissionOrDeterminationPdfService.createPdf(claim)).thenReturn(pdf);
        when(documentService.uploadToDocumentManagement(any(PDF.class),
            anyString(), any(Claim.class))).thenReturn(claim);

        CountyCourtJudgment ccjByAdmission = SampleCountyCourtJudgment.builder().ccjType(ADMISSIONS).build();
        countyCourtJudgmentService.save(ccjByAdmission, EXTERNAL_ID, AUTHORISATION);

        InOrder inOrder = inOrder(claimService, eventProducer, appInsights);

        inOrder.verify(claimService, once()).saveCountyCourtJudgment(eq(AUTHORISATION), any(), any());
        inOrder.verify(eventProducer, once()).createCountyCourtJudgmentEvent(any(Claim.class), any());
        inOrder.verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.CCJ_REQUESTED_AFTER_SETTLEMENT_BREACH),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test
    public void reDeterminationShouldFinishSuccessfullyForHappyPath() {

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
            .withCountyCourtJudgmentRequestedAt(LocalDate.of(2018, 4, 26).atStartOfDay())
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);

        InOrder inOrder = inOrder(claimService, eventProducer, appInsights);

        inOrder.verify(claimService, once()).saveReDetermination(eq(AUTHORISATION), any(),
            eq(reDetermination));
        inOrder.verify(eventProducer, once()).createRedeterminationEvent(any(Claim.class),
            eq(AUTHORISATION), eq(userDetails.getFullName()), eq(reDetermination.getPartyType()));
        inOrder.verify(appInsights, once()).trackEvent(eq(AppInsightsEvent.REDETERMINATION_REQUESTED),
            eq(REFERENCE_NUMBER), eq(claim.getReferenceNumber()));
    }

    @Test(expected = NotFoundException.class)
    public void saveThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenThrow(new NotFoundException("Claim not found by id"));

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = NotFoundException.class)
    public void reDeterminationThrowsNotFoundExceptionWhenClaimDoesNotExist() {

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenThrow(new NotFoundException("Claim not found by id"));

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void ccjByDefaultThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        String differentUser = "34234234";

        Claim claim = SampleClaim.builder().withSubmitterId(differentUser).build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void reDeterminationThrowsForbiddenActionExceptionWhenClaimWasSubmittedBySomeoneElse() {

        String differentUser = "34234234";
        UserDetails userDetails = SampleUserDetails.builder().withUserId(differentUser).build();

        Claim claim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        when(userService.getUserDetails(eq(AUTHORISATION))).thenReturn(userDetails);

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenClaimWasResponded() {

        Claim respondedClaim = SampleClaim.builder().withResponse(SampleResponse.validDefaults()).build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenUserCannotRequestCountyCourtJudgmentYet() {
        Claim respondedClaim = SampleClaim.getWithResponseDeadline(LocalDate.now().plusDays(12));

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void reDeterminationThrowsForbiddenActionExceptionWhenCountyCourtJudgmentIsNotRequestedYet() {
        Claim respondedClaim = SampleClaim.getWithDefaultResponse();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenCountyCourtJudgmentWasAlreadySubmitted() {
        Claim respondedClaim = SampleClaim.getDefault();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(respondedClaim);
        CountyCourtJudgment ccjByDefault = SampleCountyCourtJudgment.builder().ccjType(DEFAULT).build();
        countyCourtJudgmentService.save(ccjByDefault, EXTERNAL_ID, AUTHORISATION);
    }

    @Test(expected = ForbiddenActionException.class)
    public void saveThrowsForbiddenActionExceptionWhenRedeterminationWasAlreadySubmitted() {
        LocalDateTime submissionDate = LocalDate.of(2018, 4, 26).atStartOfDay();

        Claim claim = SampleClaim.builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
            .withCountyCourtJudgmentRequestedAt(submissionDate)
            .withReDetermination(reDetermination)
            .withReDeterminationRequestedAt(submissionDate)
            .build();

        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION)))
            .thenReturn(claim);

        countyCourtJudgmentService.reDetermination(reDetermination, EXTERNAL_ID, AUTHORISATION);
    }

    @Test
    public void shouldCallDocumentServiceIfCCJByAdmissionAndFeatureFlagEnabled() {
        Claim claim = SampleClaim
            .builder()
            .withResponse(SampleResponse.FullAdmission.builder().build())
            .build();
        pdf = new PDF(
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            CLAIM_ISSUE_RECEIPT
        );
        when(ccjByAdmissionOrDeterminationPdfService.createPdf(claim)).thenReturn(pdf);
        when(documentService.uploadToDocumentManagement(any(PDF.class),
            anyString(), any(Claim.class))).thenReturn(claim);
        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        CountyCourtJudgment ccj = CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.ADMISSIONS).build();
        countyCourtJudgmentService.save(ccj, EXTERNAL_ID, AUTHORISATION);
        verify(ccjByAdmissionOrDeterminationPdfService)
            .createPdf(claim);
        verify(documentService)
            .uploadToDocumentManagement(pdf, AUTHORISATION, claim);
    }

    @Test
    public void shouldCallDocumentServiceIfCCJByDeterminationAndFeatureFlagEnabled() {
        Claim claim = SampleClaim.builder()
            .withClaimantResponse(SampleClaimantResponse.ClaimantResponseAcceptation
                .builder().build())
            .build();
        pdf = new PDF(
            buildClaimIssueReceiptFileBaseName(claim.getReferenceNumber()),
            PDF_CONTENT,
            CLAIM_ISSUE_RECEIPT
        );
        when(ccjByAdmissionOrDeterminationPdfService.createPdf(claim)).thenReturn(pdf);
        when(documentService.uploadToDocumentManagement(any(PDF.class),
            anyString(), any(Claim.class))).thenReturn(claim);
        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        CountyCourtJudgment ccj = CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.DETERMINATION).build();
        countyCourtJudgmentService.save(ccj, EXTERNAL_ID, AUTHORISATION);
        verify(ccjByAdmissionOrDeterminationPdfService)
            .createPdf(claim);
        verify(documentService)
            .uploadToDocumentManagement(pdf, AUTHORISATION, claim);
    }

    @Test
    public void shouldNotCallDocumentServiceIfCCJByDefaultAndFeatureFlagEnabled() {
        CountyCourtJudgment ccj = CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT).build();
        Claim claim = SampleClaim
            .builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .build();
        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        countyCourtJudgmentService.save(ccj, EXTERNAL_ID, AUTHORISATION);
        verify(ccjByAdmissionOrDeterminationPdfService, never())
            .createPdf(any());
        verify(documentService, never())
            .uploadToDocumentManagement(any(), any(), any());
    }

    @Test
    public void shouldNotCallDocServiceIfCCJByAdmissionOrByDeterminationAndFeatureFlagDisabled() {
        countyCourtJudgmentService = new CountyCourtJudgmentService(
            claimService,
            new AuthorisationService(),
            eventProducer,
            new CountyCourtJudgmentRule(new ClaimDeadlineService()),
            userService,
            appInsights,
            caseRepository,
            ccjByAdmissionOrDeterminationPdfService,
            documentService,
            false);
        Claim claim = SampleClaim
            .builder()
            .withResponseDeadline(LocalDate.now().minusMonths(2))
            .build();
        when(claimService.getClaimByExternalId(eq(EXTERNAL_ID), eq(AUTHORISATION))).thenReturn(claim);
        CountyCourtJudgment ccj = CountyCourtJudgment.builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT).build();
        countyCourtJudgmentService.save(ccj, EXTERNAL_ID, AUTHORISATION);
        verify(ccjByAdmissionOrDeterminationPdfService, never())
            .createPdf(any());
        verify(documentService, never())
            .uploadToDocumentManagement(any(), any(), any());
    }
}
