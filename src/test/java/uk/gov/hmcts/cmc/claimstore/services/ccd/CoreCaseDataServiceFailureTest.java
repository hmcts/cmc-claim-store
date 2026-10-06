package uk.gov.hmcts.cmc.claimstore.services.ccd;

import com.google.common.collect.Maps;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.exceptions.CoreCaseDataStoreException;
import uk.gov.hmcts.cmc.claimstore.exceptions.UnprocessableEntityException;
import uk.gov.hmcts.cmc.claimstore.models.idam.User;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireService;
import uk.gov.hmcts.cmc.claimstore.services.ReferenceNumberService;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.WorkingDayIndicator;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.services.pilotcourt.PilotCourtService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
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
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PIN_GENERATION_OPERATIONS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REFER_TO_JUDGE_BY_CLAIMANT;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.CASE_TYPE_ID;
import static uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseApi.JURISDICTION_ID;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInUTC;

@ExtendWith(MockitoExtension.class)
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
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private WorkingDayIndicator workingDayIndicator;

    private final int intentionToProceedDeadlineDays = 33;

    @Mock
    private feign.Request request;
    @Mock
    private DirectionsQuestionnaireService directionsQuestionnaireService;
    @Mock
    private PilotCourtService pilotCourtService;

    private CoreCaseDataService service;

    @BeforeEach
    public void before() {
        lenient().when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        lenient().when(userService.getUserDetails(AUTHORISATION)).thenReturn(USER_DETAILS);

        lenient().when(coreCaseDataApi.startEventForCitizen(
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

        lenient().when(coreCaseDataApi.submitEventForCitizen(
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
            ccdCreateCaseService,
            caseDetailsConverter,
            intentionToProceedDeadlineDays,
            workingDayIndicator,
            directionsQuestionnaireService,
            pilotCourtService);
    }

    @Test
    public void submitPostPaymentFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
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
        });
    }

    @Test
    public void submitInitiatePaymentFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
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
        });
    }

    @Test
    public void linkDefendantFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim providedClaim = SampleClaim.getDefault();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(providedClaim);

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
        });
    }

    @Test
    public void requestMoreTimeForResponseFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim providedClaim = SampleClaim.withNoResponse();
            Claim expectedClaim = SampleClaim.claim(providedClaim.getClaimData(), "000MC001");

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(expectedClaim);

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
        });
    }

    @Test
    public void saveCountyCourtJudgmentFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim providedClaim = SampleClaim.getDefault();
            CountyCourtJudgment providedCCJ = SampleCountyCourtJudgment
                .builder()
                .ccjType(CountyCourtJudgmentType.DEFAULT)
                .build();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(providedClaim);

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
        });
    }

    @Test
    public void saveCountyCourtJudgmentFailedWithUnprocessableEntity() {
        assertThrows(UnprocessableEntityException.class, () -> {
            Claim providedClaim = SampleClaim.getDefault();
            CountyCourtJudgment providedCCJ = SampleCountyCourtJudgment
                .builder()
                .ccjType(CountyCourtJudgmentType.DEFAULT)
                .build();

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
                .thenThrow(new FeignException.UnprocessableEntity("422 from CCD", request, new byte[]{}, Map.of()));

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(providedClaim);

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
        });
    }

    @Test
    public void linkSealedClaimDocumentFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            URI sealedClaimUri = URI.create("http://localhost/sealedClaim.pdf");
            Claim claim = SampleClaim.getClaimWithSealedClaimLink(sealedClaimUri);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

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
        });
    }

    @Test
    public void saveDefendantResponseWithFullDefenceFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim providedClaim = SampleClaim.getDefault();
            Response providedResponse = SampleResponse.validDefaults();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithResponse(providedResponse));

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
        });
    }

    @Test
    public void saveDefendantResponseWithFullAdmissionFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim providedClaim = SampleClaim.getDefault();
            Response providedResponse = SampleResponse.FullAdmission.builder().build();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithResponse(providedResponse));

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
        });
    }

    @Test
    public void saveDefendantResponseWithPartAdmissionFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim providedClaim = SampleClaim.getDefault();
            Response providedResponse = SampleResponse.PartAdmission.builder().build();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithResponse(providedResponse));

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
        });
    }

    @Test
    public void saveClaimantAcceptationResponseFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Response providedResponse = SampleResponse.validDefaults();
            Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
            ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultAcceptation();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithClaimantResponse());

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
        });
    }

    @Test
    public void saveClaimantAcceptationWithCCJResponseFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Response providedResponse = SampleResponse.validDefaults();
            Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
            ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
                .builder().buildAcceptationIssueCCJWithDefendantPaymentIntention();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithClaimantResponse());

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
        });
    }

    @Test
    public void saveClaimantAcceptationWithSettlementResponseFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Response providedResponse = SampleResponse.validDefaults();
            Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
            ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
                .builder().buildAcceptationIssueSettlementWithClaimantPaymentIntention();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithClaimantResponse());

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
        });
    }

    @Test
    public void saveClaimantRejectionResponseFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Response providedResponse = SampleResponse.validDefaults();
            Claim providedClaim = SampleClaim.getWithResponse(providedResponse);
            ClaimantResponse claimantResponse = SampleClaimantResponse.validDefaultRejection();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithClaimantResponse());

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
        });
    }

    @Test
    public void saveSettlementFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Settlement providedSettlement = SampleSettlement.validDefaults();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithSettlement(providedSettlement));

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
        });
    }

    @Test
    public void reachSettlementAgreementFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Settlement providedSettlement = SampleSettlement.validDefaults();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(SampleClaim.withSettlementReached());

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
        });
    }

    @Test
    public void updateResponseDeadlineFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim providedClaim = SampleClaim.getDefault();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithResponseDeadline(FUTURE_DATE));

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
        });
    }

    @Test
    public void saveDirectionsQuestionnaireDeadlineFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Response providedResponse = SampleResponse.validDefaults();
            Claim providedClaim = SampleClaim.getWithResponse(providedResponse);

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class)))
                .thenReturn(SampleClaim.getWithResponse(providedResponse));

            service.saveDirectionsQuestionnaireDeadline(providedClaim.getId(), FUTURE_DATE, AUTHORISATION);

            verify(coreCaseDataApi, atLeastOnce()).startEventForCitizen(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), eq(DIRECTIONS_QUESTIONNAIRE_DEADLINE.getValue()));
        });
    }

    @Test
    public void updateShouldReturnCaseDetails() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            CCDCase providedCCDCase = CCDCase.builder().id(SampleClaim.CLAIM_ID).build();

            CaseDetails caseDetails = service.update(AUTHORISATION, providedCCDCase, CaseEvent.FULL_ADMISSION);

            assertNotNull(caseDetails);
        });
    }

    @Test
    public void saveCaseEventFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            ClaimantResponse claimantResponse = SampleClaimantResponse.ClaimantResponseAcceptation
                .builder().buildAcceptationReferToJudgeWithCourtDetermination();
            Claim claim = SampleClaim.getWithClaimantResponse(claimantResponse);

            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(CCDCase.builder().build());

            service.saveCaseEvent(AUTHORISATION, claim.getId(), INTERLOCUTORY_JUDGMENT);
        });
    }

    @Test
    public void saveReDeterminationFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            ReDetermination reDetermination = ReDetermination.builder()
                .explanation("Want my money sooner")
                .partyType(MadeBy.CLAIMANT)
                .build();

            Claim claim = SampleClaim.getDefault();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(SampleClaim.builder().build());

            service.saveReDetermination(AUTHORISATION, claim.getId(), reDetermination, REFER_TO_JUDGE_BY_CLAIMANT);
        });
    }

    @Test
    public void savePaidInFullSubmitEventFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim claim = SampleClaim.getDefault();
            PaidInFull paidInFull = PaidInFull.builder().moneyReceivedOn(now()).build();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

            service.savePaidInFull(claim.getId(), paidInFull, AUTHORISATION);
        });
    }

    @Test
    public void linkLetterHolderEventFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim claim = SampleClaim.getDefault();

            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(userService.authenticateAnonymousCaseWorker()).thenReturn(USER);

            String newLetterHolderId = "letter_holder_id";
            service.linkLetterHolder(claim.getId(), newLetterHolderId);
        });
    }

    @Test
    public void updateClaimSubmissionOperationIndicator() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            ClaimSubmissionOperationIndicators operationIndicators = ClaimSubmissionOperationIndicators.builder().build();
            Claim claim = SampleClaim.getDefault();

            service.saveClaimSubmissionOperationIndicators(claim.getId(), operationIndicators, AUTHORISATION,
                PIN_GENERATION_OPERATIONS);
        });
    }

    @Test
    public void addBulkPrintClaimToClaimEventFailure() {
        assertThrows(CoreCaseDataStoreException.class, () -> {
            Claim claim = SampleClaim.getDefault();

            service.addBulkPrintDetailsToClaim(
                AUTHORISATION,
                List.of(BulkPrintDetails.builder().printRequestId(UUID.randomUUID().toString()).build()),
                CaseEvent.ADD_BULK_PRINT_DETAILS,
                claim.getId());
        });
    }

}
