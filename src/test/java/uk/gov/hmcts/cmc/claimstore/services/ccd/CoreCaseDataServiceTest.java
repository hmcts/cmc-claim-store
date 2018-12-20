package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.response.CCDResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.claimantresponse.ClaimantResponseMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.offers.SettlementMapper;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.response.ResponseMapper;
import uk.gov.hmcts.cmc.ccd.domain.ccj.CCDCountyCourtJudgment;
import uk.gov.hmcts.cmc.ccd.mapper.ccj.CountyCourtJudgmentMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.services.JobSchedulerService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;
import uk.gov.hmcts.cmc.domain.models.response.CaseReference;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.offers.SampleSettlement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CaseEvent.CLAIMANT_RESPONSE_ACCEPTATION;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CaseEvent.CLAIMANT_RESPONSE_REJECTION;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.CaseEvent.TEST_SUPPORT_UPDATE;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataServiceTest {
    private static final String AUTHORISATION = "Bearer: aaa";
    private static final String EXTERNAL_ID = UUID.randomUUID().toString();
    private static final UserDetails USER_DETAILS = SampleUserDetails.builder().build();
    private static final User ANONYMOUS_USER = new User(AUTHORISATION, USER_DETAILS);
    private static final String AUTH_TOKEN = "authorisation token";
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusWeeks(4L);

    @Mock
    private CaseMapper caseMapper;
    @Mock
    private CountyCourtJudgmentMapper countyCourtJudgmentMapper;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private SettlementMapper settlementMapper;
    @Mock
    private UserService userService;
    @Mock
    private JsonMapper jsonMapper;
    @Mock
    private ReferenceNumberService referenceNumberService;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private ClaimantResponseMapper claimantResponseMapper;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseAccessApi caseAccessApi;
    @Mock
    private JobSchedulerService jobSchedulerService;
    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataCaptor;

    private CoreCaseDataService service;

    @Before
    public void before() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        when(userService.getUserDetails(AUTHORISATION)).thenReturn(USER_DETAILS);
        when(userService.authenticateAnonymousCaseWorker()).thenReturn(ANONYMOUS_USER);
        when(coreCaseDataApi.startForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            anyString()
        ))
            .thenReturn(StartEventResponse.builder()
                .caseDetails(CaseDetails.builder().build())
                .eventId("eventId")
                .token("token")
                .build());

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
                .caseDetails(CaseDetails.builder().build())
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

        this.service = new CoreCaseDataService(
            caseMapper,
            countyCourtJudgmentMapper,
            responseMapper,
            settlementMapper,
            claimantResponseMapper,
            userService,
            jsonMapper,
            referenceNumberService,
            coreCaseDataApi,
            authTokenGenerator,
            caseAccessApi,
            jobSchedulerService
        );
    }

    @Test
    public void savePrePaymentShouldReturnReferenceNumber() {
        when(coreCaseDataApi.submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        ))
            .thenReturn(CaseDetails.builder().id(SampleClaim.CLAIM_ID).build());

        CaseReference reference = service.savePrePayment(EXTERNAL_ID, AUTHORISATION);

        verify(coreCaseDataApi).submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        );

        assertNotNull(reference);
        assertEquals(SampleClaim.CLAIM_ID.toString(), reference.getCaseReference());
    }

    @Test(expected = CoreCaseDataStoreException.class)
    public void shouldThrowCCDExceptionWhenSubmitFails() {
        when(coreCaseDataApi.submitForCitizen(
            eq(AUTHORISATION),
            eq(AUTH_TOKEN),
            eq(USER_DETAILS.getId()),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(true),
            any(CaseDataContent.class)
        ))
            .thenThrow(new RuntimeException("Any runtime exception"));

        CaseReference reference = service.savePrePayment(EXTERNAL_ID, AUTHORISATION);

        assertNotNull(reference);
        assertEquals(SampleClaim.CLAIM_ID.toString(), reference.getCaseReference());
    }

    @Test
    public void submitPostPaymentShouldReturnClaim() {
        Claim providedClaim = SampleClaim.getDefault();
        Claim expectedClaim = SampleClaim.claim(null, "000MC001");

        when(caseMapper.to(providedClaim)).thenReturn(CCDCase.builder().id(SampleClaim.CLAIM_ID).build());
        when(jsonMapper.convertValue(anyMap(), eq(CCDCase.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(expectedClaim);

        Claim returnedClaim = service.submitPostPayment(AUTHORISATION, providedClaim);

        assertEquals(expectedClaim, returnedClaim);
        verify(jsonMapper).convertValue(caseDataCaptor.capture(), eq(CCDCase.class));
        assertEquals(SampleClaim.CLAIM_ID, caseDataCaptor.getValue().get("id"));
    }

    @Test
    public void requestMoreTimeForResponseShouldReturnClaim() {
        Claim providedClaim = SampleClaim.getDefault();
        Claim expectedClaim = SampleClaim.claim(null, "000MC001");

        when(jsonMapper.convertValue(anyMap(), eq(CCDCase.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(expectedClaim);

        Claim returnedClaim = service.requestMoreTimeForResponse(AUTHORISATION, providedClaim, FUTURE_DATE);

        assertEquals(expectedClaim, returnedClaim);
        verify(jsonMapper).convertValue(caseDataCaptor.capture(), eq(CCDCase.class));
        assertEquals(SampleClaim.CLAIM_ID, caseDataCaptor.getValue().get("id"));

        verify(jobSchedulerService).rescheduleEmailNotificationsForDefendantResponse(providedClaim, FUTURE_DATE);
    }

    @Test
    public void saveCountyCourtJudgmentShouldReturnCaseDetails() {
        Claim providedClaim = SampleClaim.getDefault();
        CountyCourtJudgment providedCCJ = SampleCountyCourtJudgment
            .builder()
            .ccjType(CountyCourtJudgmentType.DEFAULT)
            .build();

        when(countyCourtJudgmentMapper.to(providedCCJ)).thenReturn(
            CCDCountyCourtJudgment.builder().build());

        CaseDetails caseDetails = service.saveCountyCourtJudgment(AUTHORISATION,
            providedClaim.getId(),
            providedCCJ);

        assertNotNull(caseDetails);
    }

    @Test
    public void linkSealedClaimDocumentShouldReturnCaseDetails() {
        CaseDetails caseDetails = service.linkSealedClaimDocument(AUTHORISATION, SampleClaim.CLAIM_ID,
            URI.create("http://localhost/sealedClaim.pdf"));

        assertNotNull(caseDetails);
    }

    @Test
    public void saveDefendantResponseWithFullDefenceShouldReturnCaseDetails() {
        Claim providedClaim = SampleClaim.getDefault();
        Response providedResponse = SampleResponse.validDefaults();

        when(responseMapper.to(providedResponse)).thenReturn(CCDResponse.builder().build());

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

        when(responseMapper.to(providedResponse)).thenReturn(CCDResponse.builder().build());

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

        when(responseMapper.to(providedResponse)).thenReturn(CCDResponse.builder().build());

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

        when(claimantResponseMapper.to(claimantResponse)).thenReturn(CCDClaimantResponse.builder().build());
        when(jsonMapper.convertValue(anyMap(), eq(CCDCase.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithClaimantResponse());

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

        when(claimantResponseMapper.to(claimantResponse)).thenReturn(CCDClaimantResponse.builder().build());
        when(jsonMapper.convertValue(anyMap(), eq(CCDCase.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithClaimantResponse());

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

        when(claimantResponseMapper.to(claimantResponse)).thenReturn(CCDClaimantResponse.builder().build());
        when(jsonMapper.convertValue(anyMap(), eq(CCDCase.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithClaimantResponse());

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

        when(claimantResponseMapper.to(claimantResponse)).thenReturn(CCDClaimantResponse.builder().build());
        when(jsonMapper.convertValue(anyMap(), eq(CCDCase.class))).thenReturn(CCDCase.builder().build());
        when(caseMapper.from(any(CCDCase.class))).thenReturn(SampleClaim.getWithClaimantResponse());

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

        CaseDetails caseDetails = service.reachSettlementAgreement(
            SampleClaim.CLAIM_ID,
            providedSettlement,
            AUTHORISATION,
            CaseEvent.SETTLED_PRE_JUDGMENT);

        assertNotNull(caseDetails);
    }

    @Test
    public void updateResponseDeadlineShouldReturnCaseDetails() {
        Claim providedClaim = SampleClaim.getDefault();

        CaseDetails caseDetails = service.updateResponseDeadline(AUTHORISATION, providedClaim.getId(), FUTURE_DATE);

        assertNotNull(caseDetails);
        verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), eq(TEST_SUPPORT_UPDATE.getValue()));
    }

    @Test
    public void saveDirectionsQuestionnaireDeadlineShouldReturnCaseDetails() {
        Response providedResponse = SampleResponse.validDefaults();
        Claim providedClaim = SampleClaim.getWithResponse(providedResponse);

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
}
