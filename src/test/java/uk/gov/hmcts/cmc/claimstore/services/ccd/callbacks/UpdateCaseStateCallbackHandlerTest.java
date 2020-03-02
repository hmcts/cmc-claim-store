package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.PROCEEDS_IN_CASEMAN;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.JUDGEMENT_REQUESTED;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.OPEN;

@ExtendWith(MockitoExtension.class)
public class UpdateCaseStateCallbackHandlerTest {

    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "sealed_claim.pdf";
    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_BINARY_URL)
        .documentFileName(DOCUMENT_FILE_NAME)
        .build();
    private static final String BEARER_TOKEN = "Bearer let me in";
    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentType(CCDClaimDocumentType.ORDER_DIRECTIONS)
                .build())
            .build();
    private static final String CCD_CASE_KEY = "ccdCaseKey";
    private static final String CANNOT_UPDATE = "cannot update the case for the given claim";
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseMapper caseMapper;
    private UpdateCaseStateCallbackHandler handler;
    private CallbackParams callbackParams;
    private CallbackRequest callbackRequest;
    private CCDCase ccdCase;
    private Map<String, Object> dataMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        handler = new UpdateCaseStateCallbackHandler(
            caseDetailsConverter,
            caseMapper
        );
        ImmutableMap<String, Object> data = ImmutableMap.of("data", "existingData",
            "caseDocuments", ImmutableList.of(CLAIM_DOCUMENT));
        CaseDetails caseDetails = CaseDetails.builder()
            .id(3L)
            .data(data)
            .build();
        callbackRequest = CallbackRequest
            .builder()
            .eventId(PROCEEDS_IN_CASEMAN.getValue())
            .caseDetails(caseDetails)
            .build();
        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }

    @Nested
    @DisplayName("Proceed in Caseman")
    class ProceedInCaseMan {
        @Test
        void shouldUpdateTheCaseStateToProceedsInCaseManForCCJByAdmissionAndInJudgementRequestedState() {
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.ADMISSIONS).build();
            Claim claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj).build();
            ccdCase = CCDCase.builder().state(JUDGEMENT_REQUESTED.getValue()).build();
            dataMap.put(CCD_CASE_KEY, ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(caseMapper.to(any(Claim.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(dataMap);
            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
            verify(caseDetailsConverter)
                .convertToMap(ccdCase);
            CCDCase ccdCase = (CCDCase) response.getData().get(CCD_CASE_KEY);
            assertThat(ccdCase.getState().equals(PROCEEDS_IN_CASEMAN));
        }

        @Test
        void shouldUpdateTheCaseStateToProceedsInCaseManForCCJByDeterminationAndInJudgementRequestedState() {
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.DETERMINATION).build();
            Claim claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj).build();
            ccdCase = CCDCase.builder().state(JUDGEMENT_REQUESTED.getValue()).build();
            dataMap.put(CCD_CASE_KEY, ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(caseMapper.to(any(Claim.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(dataMap);
            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
            verify(caseDetailsConverter)
                .convertToMap(ccdCase);
            CCDCase ccdCase = (CCDCase) response.getData().get(CCD_CASE_KEY);
            assertThat(ccdCase.getState().equals(PROCEEDS_IN_CASEMAN));
        }
    }

    @Nested
    @DisplayName("Should Not Proceed in Caseman")
    class ShouldNotProceedInCaseMan {
        @Test
        void shouldNotUpdateTheCaseStateToProceedsInCaseManForCCJByDefault() {
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.DEFAULT).build();
            Claim claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj).build();
            dataMap.put(claim.getReferenceNumber(), CANNOT_UPDATE);
            ccdCase = CCDCase.builder().state(OPEN.getValue()).build();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(caseMapper.to(any(Claim.class))).thenReturn(ccdCase);
            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
            String message = (String) response.getData().get(claim.getReferenceNumber());
            verify(caseDetailsConverter, never()).convertToMap(any(CCDCase.class));
            assertThat(message.equals(CANNOT_UPDATE));
        }

        @Test
        void shouldNotUpdateTheCaseStateToProceedsInCaseManIfNoCCJRequested() {
            Claim claim = SampleClaim.builder()
                .withCountyCourtJudgment(null).build();
            dataMap.put(claim.getReferenceNumber(), CANNOT_UPDATE);
            ccdCase = CCDCase.builder().state(OPEN.getValue()).build();
            when(caseMapper.to(any(Claim.class))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
            String message = (String) response.getData().get(claim.getReferenceNumber());
            verify(caseDetailsConverter, never()).convertToMap(any(CCDCase.class));
            assertThat(message.equals(CANNOT_UPDATE));
        }

        @Test
        void shouldNotUpdateTheCaseStateToProceedsInCaseManForNotJudgementRequestedState() {
            CountyCourtJudgment ccj = CountyCourtJudgment.builder()
                .ccjType(CountyCourtJudgmentType.ADMISSIONS).build();
            Claim claim = SampleClaim.builder()
                .withCountyCourtJudgment(ccj).build();
            ccdCase = CCDCase.builder().state(OPEN.getValue()).build();
            when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
            when(caseMapper.to(any(Claim.class))).thenReturn(ccdCase);
            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
            String message = (String) response.getData().get(claim.getReferenceNumber());
            verify(caseDetailsConverter, never()).convertToMap(any(CCDCase.class));
            assertThat(message.equals(CANNOT_UPDATE));
        }
    }
}
