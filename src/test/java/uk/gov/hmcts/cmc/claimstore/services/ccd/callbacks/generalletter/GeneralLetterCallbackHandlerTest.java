package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
class GeneralLetterCallbackHandlerTest {

    @Mock
    private GeneralLetterService generalLetterService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private GeneralLetterCallbackHandler handler;

    private CallbackRequest callbackRequest;

    private CallbackParams callbackParams;
    private CCDCase ccdCase;

    private static final String EXISTING_DATA = "existingData";
    private static final String DATA = "data";
    private CaseDetails caseDetails;
    private Map<String, Object> data;
    private static final String LETTER_CONTENT = "letterContent";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final String DRAFT_LETTER_DOC_KEY = "draftLetterDoc";
    private static final String GENERAL_LETTER_TEMPLATE_ID = "generalLetterTemplateId";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final CCDDocument DRAFT_LETTER_DOC = CCDDocument.builder()
        .documentFileName(DOC_NAME)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentUrl(DOC_URL).build();
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private static final Claim claim = SampleClaim
        .builder()
        .build();
    public static final String GENERAL_DOCUMENT_NAME = "000MC001-general-letter-" + LocalDate.now() + "-1.pdf";
    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOC_URL)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentFileName(GENERAL_DOCUMENT_NAME)
        .build();
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentName(GENERAL_DOCUMENT_NAME)
                .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                .build())
            .build();
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";

    @BeforeEach
    void setUp() {
        handler = new GeneralLetterCallbackHandler(
            generalLetterService,
            GENERAL_LETTER_TEMPLATE_ID,
            caseDetailsConverter);
        String documentUrl = DOCUMENT_URI.toString();
        CCDDocument document = new CCDDocument(documentUrl, documentUrl, GENERAL_LETTER_PDF);
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .caseDocuments(ImmutableList.of(CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(document)
                    .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                    .documentName("general-letter")
                    .build())
                .build()))
            .draftLetterDoc(DRAFT_LETTER_DOC).build();
        data = new HashMap<>();
        data.put(CHANGE_CONTACT_PARTY, "claimant");
        data.put(LETTER_CONTENT, "content");
        caseDetails = CaseDetails.builder()
            .data(data)
            .build();
        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .data(data)
                .build())
            .build();
        callbackParams = CallbackParams.builder()
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
    }

    @Test
    void shouldSendForCreateAndPreview() {
        when(generalLetterService.createAndPreview(eq(ccdCase),
            eq(BEARER_TOKEN.name()),
            eq(GENERAL_LETTER_TEMPLATE_ID))).thenReturn(DOC_URL);
        AboutToStartOrSubmitCallbackResponse actualResponse =
            (AboutToStartOrSubmitCallbackResponse) handler.createAndPreview(callbackParams);
        verify(generalLetterService, once()).createAndPreview(ccdCase, BEARER_TOKEN.name(),
            GENERAL_LETTER_TEMPLATE_ID);
        assertThat(actualResponse.getData().get(DRAFT_LETTER_DOC_KEY))
            .isEqualTo(CCDDocument.builder().documentUrl(DOC_URL).build());
    }

    @Test
    void shouldSendErrorsWhenExceptionThrownForCreateAndPreview() {
        when(generalLetterService.createAndPreview(eq(ccdCase),
            eq(BEARER_TOKEN.name()),
            eq(GENERAL_LETTER_TEMPLATE_ID))).thenThrow(DocumentGenerationFailedException.class);
        AboutToStartOrSubmitCallbackResponse actualResponse =
            (AboutToStartOrSubmitCallbackResponse) handler.createAndPreview(callbackParams);
        verify(generalLetterService, once()).createAndPreview(ccdCase, BEARER_TOKEN.name(),
            GENERAL_LETTER_TEMPLATE_ID);
        assertThat(actualResponse.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void shouldSendForPrintAndUpdateCaseDocuments() throws Exception {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        Map<String, Object> dataMap = ImmutableMap.<String, Object>builder()
            .put(DATA, EXISTING_DATA)
            .build();
        CCDCase updateCCDCase = ccdCase.toBuilder()
            .caseDocuments(ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
                .addAll(ccdCase.getCaseDocuments())
                .add(CLAIM_DOCUMENT)
                .build())
            .draftLetterDoc(null)
            .generalLetterContent(null)
            .build();
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(dataMap);
        when(generalLetterService.printAndUpdateCaseDocuments(eq(ccdCase),
            eq(claim),
            eq(BEARER_TOKEN.name()),
                eq(GENERAL_DOCUMENT_NAME))).thenReturn(updateCCDCase);
        AboutToStartOrSubmitCallbackResponse actualResponse = (AboutToStartOrSubmitCallbackResponse)
            handler.printAndUpdateCaseDocuments(callbackParams);
        verify(generalLetterService, once())
            .printAndUpdateCaseDocuments(ccdCase, claim, BEARER_TOKEN.name(), GENERAL_DOCUMENT_NAME);
        assertThat(actualResponse.getData()).isEqualTo(dataMap);
    }

    @Test
    void shouldSendErrorsWhenExceptionThrownForPrintAndUpdateCaseDocuments() throws Exception {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(generalLetterService.printAndUpdateCaseDocuments(eq(ccdCase),
            eq(claim),
            eq(BEARER_TOKEN.name()),
            eq(GENERAL_DOCUMENT_NAME))).thenThrow(Exception.class);
        AboutToStartOrSubmitCallbackResponse actualResponse = (AboutToStartOrSubmitCallbackResponse)
            handler.printAndUpdateCaseDocuments(callbackParams);
        verify(generalLetterService, once())
            .printAndUpdateCaseDocuments(ccdCase, claim, BEARER_TOKEN.name(), GENERAL_DOCUMENT_NAME);
        assertThat(actualResponse.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
    }
}
