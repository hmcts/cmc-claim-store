package uk.gov.hmcts.cmc.claimstore.generalletter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.events.GeneralLetterReadyToPrintEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
class GeneralLetterServiceTest {

    private Map<String, Object> data;
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final String LETTER_CONTENT = "letterContent";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final CCDDocument DRAFT_LETTER_DOC = CCDDocument.builder()
        .documentFileName(DOC_NAME)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentUrl(DOC_URL).build();
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private static final Claim claim = SampleClaim
        .builder()
        .build();
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "sealed_claim.pdf";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    private static final String ERROR_MESSAGE =
        "There was a technical problem. Nothing has been sent. You need to try again.";
    private static final String DRAFT_LETTER_DOC_KEY = "draftLetterDoc";

    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_BINARY_URL)
        .documentFileName(DOCUMENT_FILE_NAME)
        .build();
    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentName("general-letter-2020-01-01")
                .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                .build())
            .build();
    private CCDCase ccdCase;

    private CaseDetails caseDetails;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocAssemblyResponse docAssemblyResponse;
    @Mock
    private Clock clock;

    private GeneralLetterService generalLetterService;

    @BeforeEach
    void setUp() {
        generalLetterService = new GeneralLetterService(caseDetailsConverter,
            docAssemblyService,
            publisher,
            documentManagementService,
            clock);
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
    }

    @Test
    void shouldCreateAndPreviewLetter() {
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(docAssemblyService
            .createGeneralLetter(any(CCDCase.class), anyString())).thenReturn(docAssemblyResponse);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        generalLetterService.createAndPreview(caseDetails, BEARER_TOKEN.name(), DRAFT_LETTER_DOC_KEY);
        verify(caseDetailsConverter, once()).extractCCDCase(eq(caseDetails));
        verify(docAssemblyService, once()).createGeneralLetter(eq(ccdCase), eq(BEARER_TOKEN.name()));
    }

    @Test
    void shouldSendErrorResponseWhenCreateAndPreviewLetterFails() {
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
            .thenThrow(new RuntimeException("exception"));
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            generalLetterService.createAndPreview(caseDetails, BEARER_TOKEN.name(), DRAFT_LETTER_DOC_KEY);
        assertThat(response.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    void shouldPrintAndUpdateCaseDocument() {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);
        doNothing().when(publisher).publishEvent(any(GeneralLetterReadyToPrintEvent.class));
        Map<String, Object> dataMap = ImmutableMap.<String, Object>builder()
            .put("data", "existingData")
            .put("caseDocuments", ImmutableList.of(CLAIM_DOCUMENT))
            .build();
        when(caseDetailsConverter.convertToMap(any(CCDCase.class))).thenReturn(dataMap);
        when(documentManagementService.downloadDocument(anyString(), any(ClaimDocument.class)))
            .thenReturn(PDF_BYTES);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            generalLetterService.printAndUpdateCaseDocuments(caseDetails, BEARER_TOKEN.name());
        verify(caseDetailsConverter, once()).extractCCDCase(eq(caseDetails));
        verify(caseDetailsConverter, once()).extractClaim(eq(caseDetails));
        verify(caseDetailsConverter, once()).convertToMap(any(CCDCase.class));
        verify(documentManagementService, once()).downloadDocument(eq(BEARER_TOKEN.name()), any(ClaimDocument.class));
        assertThat(response.getData()).isEqualTo(dataMap);
    }

    @Test
    void shouldSendErrorResponseWhenPrintAndUpdateCaseDocumentFails() {
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
            .thenThrow(new RuntimeException("exception"));
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            generalLetterService.printAndUpdateCaseDocuments(caseDetails, BEARER_TOKEN.name());
        assertThat(response.getErrors().get(0)).isEqualTo(ERROR_MESSAGE);
    }
}
