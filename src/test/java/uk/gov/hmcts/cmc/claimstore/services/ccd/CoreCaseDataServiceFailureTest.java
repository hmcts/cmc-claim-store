package uk.gov.hmcts.cmc.claimstore.services.ccd;

import com.google.common.collect.Maps;
import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireService;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.net.URI;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PIN_GENERATION_OPERATIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFER_TO_JUDGE_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataServiceFailureTest {
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
    private CCDCreateCaseService ccdCreateCaseService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private JobSchedulerService jobSchedulerService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private WorkingDayIndicator workingDayIndicator;

    private final int intentionToProceedDeadlineDays = 33;
    @Mock
    private feign.Request request;
    @Mock
    private DirectionsQuestionnaireService directionsQuestionnaireService;

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
            .thenThrow(new RuntimeException("Any runtime exception"));

        this.service = new CoreCaseDataService(
            caseMapper,
            userService,
            referenceNumberService,
            coreCaseDataApi,
            authTokenGenerator,
            jobSchedulerService,
            ccdCreateCaseService,
            caseDetailsConverter,
            intentionToProceedDeadlineDays,
            workingDayIndicator,
            directionsQuestionnaireService
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void submitPostPaymentFailure() {
        Claim providedClaim = SampleClaim.getDefault();
        when(caseMapper.to(providedClaim)).thenReturn(CCDCase.builder().id(SampleClaim.CLAIM_ID).build());

        service.createNewCase(USER, providedClaim);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = ConflictException.class)
    public void createNewCaseBubblesUpConflictException() {
        Claim providedClaim = SampleClaim.getDefaultForLegal();
        User solicitorUser = new User(AUTHORISATION, SampleUserDetails.builder().withRoles("solicitor").build());
        when(caseMapper.to(providedClaim)).thenReturn(CCDCase.builder().id(SampleClaim.CLAIM_ID).build());

        when(ccdCreateCaseService.startCreate(
            eq(AUTHORISATION),
            any(EventRequestData.class),
            eq(solicitorUser.isRepresented())
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
            eq(solicitorUser.isRepresented())
        ))
            .thenThrow(new FeignException.Conflict("Status 409 while creating the case", request, null));

        service.createRepresentedClaim(solicitorUser, providedClaim);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void submitInitiatePaymentFailure() {
        Claim providedClaim = SampleClaim.getDefault();
        when(caseMapper.to(providedClaim)).thenReturn(CCDCase.builder().id(SampleClaim.CLAIM_ID).build());

        service.initiatePaymentForCitizenCase(USER, providedClaim);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void linkDefendantFailure() {
        Claim providedClaim = SampleClaim.getDefault();
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(providedClaim);

        service.linkDefendant(AUTHORISATION,
            providedClaim.getId(),
            providedClaim.getDefendantId(),
            providedClaim.getDefendantEmail(),
            CaseEvent.LINK_DEFENDANT);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void requestMoreTimeForResponseFailure() {
        Claim providedClaim = SampleClaim.withNoResponse();
        Claim expectedClaim = SampleClaim.claim(providedClaim.getClaimData(), "000MC001");

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(expectedClaim);

        service.requestMoreTimeForResponse(AUTHORISATION, providedClaim, FUTURE_DATE);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveCountyCourtJudgmentFailure() {
        Claim providedClaim = SampleClaim.getDefault();
        CountyCourtJudgment providedCCJ = SampleCountyCourtJudgment
            .builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(providedClaim);

        service.saveCountyCourtJudgment(AUTHORISATION,
            providedClaim.getId(),
            providedCCJ);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void linkSealedClaimDocumentFailure() {

        URI sealedClaimUri = URI.create("http://localhost/sealedClaim.pdf");
        Claim claim = SampleClaim.getClaimWithSealedClaimLink(sealedClaimUri);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

        service.saveClaimDocuments(AUTHORISATION,
            SampleClaim.CLAIM_ID,
            claim.getClaimDocumentCollection().orElse(new ClaimDocumentCollection()),
            ClaimDocumentType.CLAIM_ISSUE_RECEIPT);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveDefendantResponseWithFullDefenceFailure() {
        Claim providedClaim = SampleClaim.getDefault();
        Response providedResponse = SampleResponse.validDefaults();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponse(providedResponse));

        service.saveDefendantResponse(providedClaim.getId(),
            "defendant@email.com",
            providedResponse,
            AUTHORISATION
        );

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveDefendantResponseWithFullAdmissionFailure() {
        Claim providedClaim = SampleClaim.getDefault();
        Response providedResponse = SampleResponse.FullAdmission.builder().build();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponse(providedResponse));

        service.saveDefendantResponse(providedClaim.getId(),
            "defendant@email.com",
            providedResponse,
            AUTHORISATION
        );

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveDefendantResponseWithPartAdmissionFailure() {
        Claim providedClaim = SampleClaim.getDefault();
        Response providedResponse = SampleResponse.PartAdmission.builder().build();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponse(providedResponse));

        service.saveDefendantResponse(providedClaim.getId(),
            "defendant@email.com",
            providedResponse,
            AUTHORISATION
        );

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveClaimantAcceptationResponseFailure() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultAcceptation();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithClaimantResponse());

        service.saveClaimantResponse(providedClaim.getId(),
            claimantResponse,
            AUTHORISATION
        );

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveClaimantAcceptationWithCCJResponseFailure() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder().buildAcceptationIssueCCJWithDefendantPaymentIntention();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithClaimantResponse());

        service.saveClaimantResponse(providedClaim.getId(),
            claimantResponse,
            AUTHORISATION
        );

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveClaimantAcceptationWithSettlementResponseFailure() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder().buildAcceptationIssueSettlementWithClaimantPaymentIntention();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithClaimantResponse());

        service.saveClaimantResponse(providedClaim.getId(), claimantResponse, AUTHORISATION);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveClaimantRejectionResponseFailure() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
        ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithClaimantResponse());

        service.saveClaimantResponse(providedClaim.getId(),
            claimantResponse,
            AUTHORISATION
        );

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveSettlementFailure() {
        Settlement providedSettlement = SampleSettlement.validDefaults();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithSettlement(providedSettlement));

        service.saveSettlement(
            SampleClaim.CLAIM_ID,
            providedSettlement,
            AUTHORISATION,
            CaseEvent.SETTLED_PRE_JUDGMENT
        );

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void reachSettlementAgreementFailure() {
        Settlement providedSettlement = SampleSettlement.validDefaults();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.withSettlementReached());

        service.reachSettlementAgreement(
            SampleClaim.CLAIM_ID,
            providedSettlement,
            nowInUTC(),
            AUTHORISATION,
            CaseEvent.SETTLED_PRE_JUDGMENT);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void updateResponseDeadlineFailure() {
        Claim providedClaim = SampleClaim.getDefault();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponseDeadline(FUTURE_DATE));

        service.updateResponseDeadline(AUTHORISATION, providedClaim.getId(), FUTURE_DATE);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveDirectionsQuestionnaireDeadlineFailure() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithResponse(providedResponse));

        service.saveDirectionsQuestionnaireDeadline(providedClaim.getId(), FUTURE_DATE, AUTHORISATION);

        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(DIRECTIONS_QUESTIONNAIRE_DEADLINE.getValue()));
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void updateShouldReturnCaseDetails() {
        CCDCase providedCCDCase = CCDCase.builder().id(SampleClaim.CLAIM_ID).build();

        CaseDetails caseDetails = service.update(AUTHORISATION, providedCCDCase, CaseEvent.FULL_ADMISSION);

        assertNotNull(caseDetails);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveCaseEventFailure() {
        ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
            .builder().buildAcceptationReferToJudgeWithCourtDetermination();
        Claim claim = SampleClaim.getWithClaimantResponse(claimantResponse);

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());

        service.saveCaseEvent(AUTHORISATION, claim.getId(), INTERLOCUTORY_JUDGMENT);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void saveReDeterminationFailure() {
        ReDetermination reDetermination = ReDetermination.builder()
            .explanation("Want my money sooner")
            .partyType(MadeBy.CLAIMANT)
            .build();

        Claim claim = SampleClaim.getDefault();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());

        service.saveReDetermination(AUTHORISATION, claim.getId(), reDetermination, REFER_TO_JUDGE_BY_CLAIMANT);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void savePaidInFullSubmitEventFailure() {
        Claim claim = SampleClaim.getDefault();
        PaidInFull paidInFull = PaidInFull.builder().moneyReceivedOn(now()).build();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);

        service.savePaidInFull(claim.getId(), paidInFull, AUTHORISATION);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void linkLetterHolderEventFailure() {
        Claim claim = SampleClaim.getDefault();

        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(claim);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);

        String newLetterHolderId = "letter_holder_id";
        service.linkLetterHolder(claim.getId(), newLetterHolderId);
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void updateClaimSubmissionOperationIndicator() {

        ClaimSubmissionOperationIndicators operationIndicators = ClaimSubmissionOperationIndicators.builder().build();
        Claim claim = SampleClaim.getDefault();

        service.saveClaimSubmissionOperationIndicators(claim.getId(), operationIndicators, AUTHORISATION,
            PIN_GENERATION_OPERATIONS);
    }

}
