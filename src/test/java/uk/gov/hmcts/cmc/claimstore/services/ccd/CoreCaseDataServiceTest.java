package uk.gov.hmcts.cmc.claimstore.services.ccd;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.IntentionToProceedDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleReviewOrder;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIMANT_RESPONSE_ACCEPTATION;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIMANT_RESPONSE_REJECTION;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LINK_LETTER_HOLDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ORDER_REVIEW_REQUESTED;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PIN_GENERATION_OPERATIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFER_TO_JUDGE_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SETTLED_PRE_JUDGMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.TEST_SUPPORT_UPDATE;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.getWithClaimantResponse;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataServiceTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder().build();
    private static final User USER = new User(AUTHORISATION, USER_DETAILS);
    private static final String AUTH_TOKEN = "authorisation token";
    private static final LocalDate FUTURE_DATE = now().plusWeeks(4);

    @Mock
    private CaseMapper caseMapper;
    @Mock
    private UserService userService;
    @Mock
    private ReferenceNumberService referenceNumberService;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CCDCreateCaseService ccdCreateCaseService;
    @Mock
    private JobSchedulerService jobSchedulerService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private IntentionToProceedDeadlineCalculator intentionToProceedDeadlineCalculator;

    private CoreCaseDataService service;

    @Before
    public void before() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(USER_DETAILS);

        when(coreCaseDataApi.startEventForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(SampleClaim.CLAIM_ID.toString()),
            anyString()
        ))
            .thenReturn(StartEventResponse.builder()
                .caseDetails(CaseDetails.builder().data(Maps.newHashMap()).build())
                .eventId("eventId")
                .token("token")
                .build());

        when(coreCaseDataApi.submitEventForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(SampleClaim.CLAIM_ID.toString()),
            anyBoolean(),
            any()
        ))
            .thenReturn(CaseDetails.builder()
                .id(SampleClaim.CLAIM_ID)
                .data(new HashMap<>())
                .build());

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());

        this.service = new CoreCaseDataService(
            caseMapper,
            userService,
            referenceNumberService,
            coreCaseDataApi,
            authTokenGenerator,
            jobSchedulerService,
            ccdCreateCaseService,
            caseDetailsConverter,
            intentionToProceedDeadlineCalculator
        );
    }

    @Test
    public void submitClaimShouldReturnClaim() {
        Claim providedClaim = SampleClaim.getDefault();
        Claim expectedClaim = SampleClaim.claim(providedClaim.getClaimData(), "000MC001");

        when(ccdCreateCaseService.startCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            eq(false)
        ))
            .thenReturn(StartEventResponse.builder()
                .caseDetails(CaseDetails.builder().build())
                .eventId("eventId")
                .token("token")
                .build());

        when(ccdCreateCaseService.submitCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            any(CaseDataContent.class),
            eq(false)
        ))
            .thenReturn(CaseDetails.builder()
                .id(SampleClaim.CLAIM_ID)
                .data(new HashMap<>())
                .build());

        when(caseMapper.to(providedClaim)).thenReturn(CCDCase.builder().id(SampleClaim.CLAIM_ID).build());
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(expectedClaim);

        Claim returnedClaim = service.createNewCase(USER, providedClaim);

        assertEquals(expectedClaim, returnedClaim);
    }

    @Test
    public void submitRepresentedClaimShouldReturnLegalRepClaim() {
        Claim providedLegalRepClaim = SampleClaim.getDefaultForLegal();
        Claim expectedLegalRepClaim = SampleClaim.claim(providedLegalRepClaim.getClaimData(), "012LR345");

        when(ccdCreateCaseService.startCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            eq(false)
        ))
            .thenReturn(StartEventResponse.builder()
                .caseDetails(CaseDetails.builder().build())
                .eventId("eventId")
                .token("token")
                .build());

        when(ccdCreateCaseService.submitCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            any(CaseDataContent.class),
            eq(false)
        ))
            .thenReturn(CaseDetails.builder()
                .id(SampleClaim.CLAIM_ID)
                .data(new HashMap<>())
                .build());

        when(caseMapper.to(providedLegalRepClaim)).thenReturn(CCDCase.builder().id(SampleClaim.CLAIM_ID).build());
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(expectedLegalRepClaim);

        Claim returnedLegalRepClaim = service.createNewCase(USER, providedLegalRepClaim);

        assertEquals(expectedLegalRepClaim, returnedLegalRepClaim);
    }

    @Test
    public void submitCitizenClaimShouldReturnClaim() {
        Claim providedClaim = SampleClaim.getDefault();
        Claim expectedClaim = SampleClaim.claim(providedClaim.getClaimData(), "000MC001");

        when(ccdCreateCaseService.startCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            eq(false)
        ))
            .thenReturn(StartEventResponse.builder()
                .caseDetails(CaseDetails.builder().build())
                .eventId("eventId")
                .token("token")
                .build());

        when(ccdCreateCaseService.submitCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            any(CaseDataContent.class),
            eq(false)
        ))
            .thenReturn(CaseDetails.builder()
                .id(SampleClaim.CLAIM_ID)
                .data(new HashMap<>())
                .build());

        when(caseMapper.to(providedClaim)).thenReturn(CCDCase.builder().id(SampleClaim.CLAIM_ID).build());
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(expectedClaim);

        Claim returnedClaim = service.createNewCitizenCase(USER, providedClaim);

        assertEquals(expectedClaim, returnedClaim);
    }

    @Test
    public void submitClaimShouldNotCallAuthoriseIfLetterHolderIsNull() {
        Claim providedClaim = SampleClaim.getDefault().toBuilder().letterHolderId(null).build();

        when(ccdCreateCaseService.startCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            eq(false)
        ))
            .thenReturn(StartEventResponse.builder()
                .caseDetails(CaseDetails.builder().build())
                .eventId("eventId")
                .token("token")
                .build());

        when(ccdCreateCaseService.submitCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            any(CaseDataContent.class),
            eq(false)
        ))
            .thenReturn(CaseDetails.builder()
                .id(SampleClaim.CLAIM_ID)
                .data(new HashMap<>())
                .build());

        when(caseMapper.to(providedClaim)).thenReturn(CCDCase.builder().id(SampleClaim.CLAIM_ID).build());

        service.createNewCase(USER, providedClaim);

        verify(ccdCreateCaseService, never()).grantAccessToCase(any(), any());
    }

    @Test
    public void linkDefendantShouldBeSuccessful() {
        Claim providedClaim = SampleClaim.getDefault();
        when(caseMapper.from(any(CCDCase.class))).thenReturn(providedClaim);

        CaseDetails caseDetails = service.linkDefendant(AUTHORISATION,
            providedClaim.getId(),
            providedClaim.getDefendantId(),
            providedClaim.getDefendantEmail(),
            CaseEvent.LINK_DEFENDANT);

        assertNotNull(caseDetails);
        verify(caseDetailsConverter).extractCCDCase(any(CaseDetails.class));
    }

    @Test
    public void requestMoreTimeForResponseShouldReturnClaim() {
        Claim providedClaim = SampleClaim.withNoResponse();
        Claim expectedClaim = SampleClaim.claim(providedClaim.getClaimData(), "000MC001");

        when(caseMapper.from(any(CCDCase.class))).thenReturn(expectedClaim);
        when(caseDetailsConverter.extractClaim(any((CaseDetails.class)))).thenReturn(expectedClaim);

        Claim returnedClaim = service.requestMoreTimeForResponse(AUTHORISATION, providedClaim, FUTURE_DATE);

        assertEquals(expectedClaim, returnedClaim);
        verify(caseDetailsConverter, atLeast(1)).extractCCDCase(any(CaseDetails.class));
        assertEquals(SampleClaim.CLAIM_ID, returnedClaim.getId());

        verify(jobSchedulerService).rescheduleEmailNotificationsForDefendantResponse(providedClaim, FUTURE_DATE);
    }

    @Test
    public void saveCountyCourtJudgmentShouldReturnCaseDetails() {
        Claim providedClaim = SampleClaim.getDefault();
        CountyCourtJudgment providedCCJ = SampleCountyCourtJudgment
            .builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(providedClaim);

        CaseDetails caseDetails = service.saveCountyCourtJudgment(AUTHORISATION,
            providedClaim.getId(),
            providedCCJ);

        assertNotNull(caseDetails);
        verify(caseDetailsConverter).extractCCDCase(any(CaseDetails.class));
    }

    @Test
    public void linkSealedClaimDocumentShouldReturnCaseDetails() {

        URI sealedClaimUri = URI.create("http://localhost/sealedClaim.pdf");
        Claim claim = SampleClaim.getClaimWithSealedClaimLink(sealedClaimUri);
        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);
        when(caseDetailsConverter.extractClaim(any((CaseDetails.class)))).thenReturn(claim);

        Claim updatedClaim = service.saveClaimDocuments(AUTHORISATION,
            SampleClaim.CLAIM_ID,
            claim.getClaimDocumentCollection().orElse(new ClaimDocumentCollection()),
            null);

        assertNotNull(updatedClaim);
    }

    @Test
    public void saveDefendantResponseWithFullDefenceShouldReturnCaseDetails() {
        Claim providedClaim = SampleClaim.getDefault();
        Response providedResponse = SampleResponse.validDefaults();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponse(providedResponse));

        CaseDetails caseDetails = service.saveDefendantResponse(providedClaim.getId(),
            "defendant@email.com",
            providedResponse,
            AUTHORISATION
        );

        assertNotNull(caseDetails);
    }

    @Test
    public void saveDefendantResponseWithFullAdmissionShouldReturnCaseDetails() {
        Claim providedClaim = SampleClaim.getDefault();
        Response providedResponse = SampleResponse.FullAdmission.builder().build();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponse(providedResponse));

        CaseDetails caseDetails = service.saveDefendantResponse(providedClaim.getId(),
            "defendant@email.com",
            providedResponse,
            AUTHORISATION
        );

        assertNotNull(caseDetails);
    }

    @Test
    public void saveDefendantResponseWithPartAdmissionShouldReturnCaseDetails() {
        Claim providedClaim = SampleClaim.getDefault();
        Response providedResponse = SampleResponse.PartAdmission.builder().build();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponse(providedResponse));

        CaseDetails caseDetails = service.saveDefendantResponse(providedClaim.getId(),
            "defendant@email.com",
            providedResponse,
            AUTHORISATION
        );

        assertNotNull(caseDetails);
    }

    @Test
    public void saveClaimantAcceptationResponseShouldReturnClaim() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultAcceptation();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(getWithClaimantResponse());
        when(caseDetailsConverter.extractClaim(any((CaseDetails.class)))).thenReturn(getWithClaimantResponse());

        Claim claim = service.saveClaimantResponse(providedClaim.getId(),
            claimantResponse,
            AUTHORISATION
        );

        assertThat(claim).isNotNull();
        assertThat(claim.getClaimantResponse()).isPresent();
        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(CLAIMANT_RESPONSE_ACCEPTATION.getValue()));
    }

    @Test
    public void saveClaimantAcceptationWithCCJResponseShouldReturnClaim() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder().buildAcceptationIssueCCJWithDefendantPaymentIntention();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(getWithClaimantResponse());
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
            .thenReturn(getWithClaimantResponse());

        Claim claim = service.saveClaimantResponse(providedClaim.getId(),
            claimantResponse,
            AUTHORISATION
        );

        assertThat(claim).isNotNull();
        assertThat(claim.getClaimantResponse()).isPresent();
        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(CLAIMANT_RESPONSE_ACCEPTATION.getValue()));
    }

    @Test
    public void saveClaimantAcceptationWithSettlementResponseShouldReturnClaim() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder().buildAcceptationIssueSettlementWithClaimantPaymentIntention();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(getWithClaimantResponse());
        when(caseDetailsConverter.extractClaim(any((CaseDetails.class)))).thenReturn(getWithClaimantResponse());

        Claim claim = service.saveClaimantResponse(providedClaim.getId(), claimantResponse, AUTHORISATION);

        assertThat(claim).isNotNull();
        assertThat(claim.getClaimantResponse()).isPresent();
        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(CLAIMANT_RESPONSE_ACCEPTATION.getValue()));
    }

    @Test
    public void saveClaimantRejectionResponseShouldReturnClaim() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(getWithClaimantResponse());
        when(caseDetailsConverter.extractClaim(any((CaseDetails.class)))).thenReturn(getWithClaimantResponse());

        Claim claim = service.saveClaimantResponse(providedClaim.getId(),
            claimantResponse,
            AUTHORISATION
        );

        assertThat(claim).isNotNull();
        assertThat(claim.getClaimantResponse()).isPresent();
        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(CLAIMANT_RESPONSE_REJECTION.getValue()));
    }

    @Test
    public void saveSettlementShouldReturnCaseDetails() {
        Settlement providedSettlement = SampleSettlement.validDefaults();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithSettlement(providedSettlement));

        CaseDetails caseDetails = service.saveSettlement(
            SampleClaim.CLAIM_ID,
            providedSettlement,
            AUTHORISATION,
            CaseEvent.SETTLED_PRE_JUDGMENT
        );

        assertNotNull(caseDetails);
    }

    @Test
    public void reachSettlementAgreementShouldReturnCaseDetails() {
        Settlement providedSettlement = SampleSettlement.validDefaults();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.withSettlementReached());

        CaseDetails caseDetails = service.reachSettlementAgreement(
            SampleClaim.CLAIM_ID,
            providedSettlement,
            nowInUTC(),
            AUTHORISATION,
            CaseEvent.SETTLED_PRE_JUDGMENT);

        assertNotNull(caseDetails);
    }

    @Test
    public void updateResponseDeadlineShouldReturnCaseDetails() {
        Claim providedClaim = SampleClaim.getDefault();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponseDeadline(FUTURE_DATE));

        CaseDetails caseDetails = service.updateResponseDeadline(AUTHORISATION, providedClaim.getId(), FUTURE_DATE);

        assertNotNull(caseDetails);
        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(TEST_SUPPORT_UPDATE.getValue()));
    }

    @Test
    public void saveDirectionsQuestionnaireDeadlineShouldReturnCaseDetails() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);

        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponse(providedResponse));

        service.saveDirectionsQuestionnaireDeadline(providedClaim.getId(), FUTURE_DATE, AUTHORISATION);

        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(DIRECTIONS_QUESTIONNAIRE_DEADLINE.getValue()));
    }

    @Test
    public void updateShouldReturnCaseDetails() {
        CCDCase providedCCDCase = CCDCase.builder().id(SampleClaim.CLAIM_ID).build();

        CaseDetails caseDetails = service.update(AUTHORISATION, providedCCDCase, CaseEvent.FULL_ADMISSION);

        assertNotNull(caseDetails);
    }

    @Test
    public void saveCaseEventInterlocatoryJudgement() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder().buildAcceptationReferToJudgeWithCourtDetermination();
        Claim claim = getWithClaimantResponse(claimantResponse);

        service.saveCaseEvent(AUTHORISATION, claim.getId(), INTERLOCUTORY_JUDGMENT);

        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(INTERLOCUTORY_JUDGMENT.getValue()));
    }

    @Test
    public void saveCaseReDetermination() {
        ReDetermination reDetermination = ReDetermination.builder()
            .explanation("Want my money sooner")
            .partyType(MadeBy.CLAIMANT)
            .build();

        Claim claim = SampleClaim.getDefault();

        when(caseMapper.from(any(CCDCase.class)))
            .thenReturn(SampleClaim.builder().withReDetermination(reDetermination).build());

        service.saveReDetermination(AUTHORISATION, claim.getId(), reDetermination, REFER_TO_JUDGE_BY_CLAIMANT);

        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(REFER_TO_JUDGE_BY_CLAIMANT.getValue()));
    }

    @Test
    public void savePaidInFull() {
        Claim claim = SampleClaim.getDefault();
        PaidInFull paidInFull = PaidInFull.builder().moneyReceivedOn(now()).build();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

        service.savePaidInFull(claim.getId(), paidInFull, AUTHORISATION);

        verify(coreCaseDataApi).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(SETTLED_PRE_JUDGMENT.getValue()));
    }

    @Test
    public void linkLetterHolderId() {
        Claim claim = SampleClaim.getDefault();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);

        String newLetterHolderId = "letter_holder_id";
        service.linkLetterHolder(claim.getId(), newLetterHolderId);

        verify(coreCaseDataApi).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(LINK_LETTER_HOLDER.getValue()));

        verify(ccdCreateCaseService).removeAccessToCase(eq(claim.getId().toString()), eq(claim.getLetterHolderId()));
        verify(ccdCreateCaseService).grantAccessToCase(eq(claim.getId().toString()), eq(newLetterHolderId));
    }

    @Test
    public void updateClaimSubmissionOperationIndicator() {

        ClaimSubmissionOperationIndicators operationIndicators = ClaimSubmissionOperationIndicators.builder().build();
        Claim claim = SampleClaim.getDefault();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

        service.saveClaimSubmissionOperationIndicators(claim.getId(), operationIndicators, AUTHORISATION,
            PIN_GENERATION_OPERATIONS);

        verify(coreCaseDataApi).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(PIN_GENERATION_OPERATIONS.getValue()));
    }

    @Test
    public void saveReviewOrderShouldBeSuccessful() {
        Claim claim = SampleClaim.getDefault();
        ReviewOrder reviewOrder = SampleReviewOrder.getDefault();

        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

        service.saveReviewOrder(claim.getId(), reviewOrder, AUTHORISATION);

        verify(coreCaseDataApi).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(ORDER_REVIEW_REQUESTED.getValue()));
        verify(coreCaseDataApi).submitEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(true), any(CaseDataContent.class));
    }
}
