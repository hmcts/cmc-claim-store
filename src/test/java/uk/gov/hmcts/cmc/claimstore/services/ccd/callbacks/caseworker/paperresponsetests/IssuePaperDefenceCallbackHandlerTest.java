package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperresponsetests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.rules.ClaimDeadlineService;
import uk.gov.hmcts.cmc.claimstore.services.IssueDateCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.DocumentPublishService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.IssuePaperDefenceCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.IssuePaperResponseNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@ExtendWith(MockitoExtension.class)
 class IssuePaperDefenceCallbackHandlerTest {
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOC_URL)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentFileName(DOC_NAME)
        .build();
    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                .build())
            .build();
    private static final String ERROR_MESSAGE = "There was a technical problem. Nothing has been sent."
        + " You need to try again.";
    private static final String AUTHORISATION = "auth";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private IssueDateCalculator issueDateCalculator;
    @Mock
    private DocumentPublishService documentPublishService;
    @Mock
    private IssuePaperResponseNotificationService issuePaperResponseNotificationService;
    @Mock
    private ClaimDeadlineService claimDeadlineService;

    private Claim claim;
    private CallbackRequest callbackRequest;
    private IssuePaperDefenceCallbackHandler issuePaperDefenceCallbackHandler;
    private CCDCase ccdCase;
    private CallbackParams callbackParams;

    @BeforeEach
    void setUp() {
        issuePaperDefenceCallbackHandler = new IssuePaperDefenceCallbackHandler(
            caseDetailsConverter,
            responseDeadlineCalculator,
            issueDateCalculator,
            issuePaperResponseNotificationService,
            documentPublishService,
            claimDeadlineService
        );
        claim = Claim.builder()
            .claimData(SampleClaimData.builder().build())
            .defendantEmail("email@email.com")
            .defendantId("id")
            .submitterEmail("email@email.com")
            .referenceNumber("ref. number")
            .build();
        //not working??
    }

    @Test
    void shouldHandleAboutToSubmitCallback() {
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .respondents(ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                    .build()
            ))
            .applicants(List.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
                    .build()
            ))
            .build();

        LocalDate date = LocalDate.now();
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculateResponseDeadline(any(LocalDate.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
            .thenReturn(date);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            CallbackRequest.builder()
                .eventId(CaseEvent.ISSUE_PAPER_DEFENSE_FORMS.getValue())
                .caseDetails(caseDetails)
                .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();

        issuePaperDefenceCallbackHandler.handle(callbackParams);

        verify(documentPublishService).publishDocuments(any(CCDCase.class), any(Claim.class), eq(AUTHORISATION),
            eq(date));
        verify(issuePaperResponseNotificationService).notifyClaimant(any(Claim.class));
    }

    @Test
    void shouldSendErrorsWhenExceptionThrownForCallback() {
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .respondents(ImmutableList.of(
                CCDCollectionElement.<CCDRespondent>builder()
                    .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                    .build()
            ))
            .applicants(List.of(
                CCDCollectionElement.<CCDApplicant>builder()
                    .value(SampleData.getCCDApplicantIndividual())
                    .build()
            ))
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        callbackRequest =
            CallbackRequest.builder()
                .eventId(CaseEvent.ISSUE_PAPER_DEFENSE_FORMS.getValue())
                .caseDetails(caseDetails)
                .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
        LocalDate date = LocalDate.now();
        when(issueDateCalculator.calculateIssueDay(any(LocalDateTime.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculateResponseDeadline(any(LocalDate.class))).thenReturn(date);
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(any(LocalDate.class)))
            .thenReturn(date);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(documentPublishService.publishDocuments(ccdCase, claim, AUTHORISATION, LocalDate.now()))
            .thenThrow(DocumentGenerationFailedException.class);
        AboutToStartOrSubmitCallbackResponse actualResponse =
            (AboutToStartOrSubmitCallbackResponse) issuePaperDefenceCallbackHandler.handle(callbackParams);
        assertThat(actualResponse.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void shouldHaveCorrectCaseworkerRole() {
        assertThat(issuePaperDefenceCallbackHandler.getSupportedRoles()).containsOnly(CASEWORKER);
    }

}

