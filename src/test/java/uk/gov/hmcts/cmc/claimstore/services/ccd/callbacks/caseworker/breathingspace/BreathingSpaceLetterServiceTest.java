package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.breathingspace;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintRequestType;
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.bulkprint.PrintRequestType.PIN_LETTER_TO_DEFENDANT;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
class BreathingSpaceLetterServiceTest {

    public static final String BREATHING_SPACE_LETTER_TEMPLATE_ID = "breathingSpaceEnteredTemplateID";
    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final CCDDocument DRAFT_LETTER_DOC = CCDDocument.builder()
        .documentFileName(DOC_NAME)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentUrl(DOC_URL).build();
    private static final Claim claim = SampleClaim.builder().build();
    private static final byte[] PDF_BYTES = new byte[]{1, 2, 3, 4};
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");
    private static final Map<String, Object> VALUES = Collections.emptyMap();
    private static final Document BREATHING_SPACE_DOCUMENT = new Document(DOC_URL, VALUES);
    private final BulkPrintDetails bulkPrintDetails = BulkPrintDetails.builder()
        .printRequestType(PIN_LETTER_TO_DEFENDANT).printRequestId("requestId").build();
    private final Claim claimWithBulkPrintDetails
        = claim.toBuilder().bulkPrintDetails(List.of(bulkPrintDetails)).build();

    @Mock
    DocumentManagementService documentManagementService;
    private CCDCase ccdCase;
    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    @Mock
    private DocAssemblyResponse docAssemblyResponse;
    @Mock
    private PrintableDocumentService printableDocumentService;
    @Mock
    private PrintService bulkPrintService;
    @Mock
    private ClaimService claimService;
    @Mock
    private GeneralLetterService generalLetterService;
    private BreathingSpaceLetterService breathingSpaceLetterService;

    @BeforeEach
    void setUp() {
        breathingSpaceLetterService = new BreathingSpaceLetterService(
            docAssemblyService,
            docAssemblyTemplateBodyMapper, printableDocumentService, bulkPrintService, claimService,
            documentManagementService, generalLetterService);

        String documentUrl = DOCUMENT_URI.toString();
        CCDDocument document = new CCDDocument(documentUrl, documentUrl, GENERAL_LETTER_PDF);
        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .caseDocuments(ImmutableList.of(CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentLink(document)
                    .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                    .documentName("000MC001-breathing-space-entered.pdf")
                    .build())
                .build()))
            .draftLetterDoc(DRAFT_LETTER_DOC).build();
    }

    @Test
    void shouldCreateAndPreviewLetter() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.breathingSpaceLetter(any(CCDCase.class)))
            .thenReturn(docAssemblyTemplateBody);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        when(printableDocumentService.pdf(any(CCDDocument.class), anyString())).thenReturn(PDF_BYTES);
        when(printableDocumentService.process(any(CCDDocument.class), anyString()))
            .thenReturn(BREATHING_SPACE_DOCUMENT);
        when(bulkPrintService
            .printPdf(any(Claim.class), any(),
                any(BulkPrintRequestType.class),
                any(String.class)))
            .thenReturn(bulkPrintDetails);

        when(claimService.addBulkPrintDetails(any(String.class), any(), any(CaseEvent.class), any(Claim.class)))
            .thenReturn(claimWithBulkPrintDetails);

        breathingSpaceLetterService.sendLetterToDefendant(ccdCase, claim, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID);

        verify(docAssemblyService, once()).renderTemplate(ccdCase, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID, docAssemblyTemplateBody);
    }

    @Test
    void shouldPrintLetter() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.breathingSpaceLetter(any(CCDCase.class)))
            .thenReturn(docAssemblyTemplateBody);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        when(printableDocumentService.pdf(any(CCDDocument.class), anyString())).thenReturn(PDF_BYTES);
        when(printableDocumentService.process(any(CCDDocument.class), anyString()))
            .thenReturn(BREATHING_SPACE_DOCUMENT);
        when(bulkPrintService
            .printPdf(any(Claim.class), any(),
                any(BulkPrintRequestType.class),
                any(String.class)))
            .thenReturn(bulkPrintDetails);

        when(claimService.addBulkPrintDetails(any(String.class), any(), any(CaseEvent.class), any(Claim.class)))
            .thenReturn(claimWithBulkPrintDetails);

        breathingSpaceLetterService.sendLetterToDefendant(ccdCase, claim, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID);

        verify(bulkPrintService, once()).printPdf(any(Claim.class), any(),
            any(BulkPrintRequestType.class),
            any(String.class));

        verify(claimService, once()).addBulkPrintDetails(any(String.class), any(), any(CaseEvent.class),
            any(Claim.class));

    }

    @Test
    void shouldUploadLetter() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.breathingSpaceLetter(any(CCDCase.class)))
            .thenReturn(docAssemblyTemplateBody);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        when(printableDocumentService.pdf(any(CCDDocument.class), anyString())).thenReturn(PDF_BYTES);
        when(printableDocumentService.process(any(CCDDocument.class), anyString()))
            .thenReturn(BREATHING_SPACE_DOCUMENT);
        when(bulkPrintService
            .printPdf(any(Claim.class), any(),
                any(BulkPrintRequestType.class),
                any(String.class)))
            .thenReturn(bulkPrintDetails);

        when(claimService.addBulkPrintDetails(any(String.class), any(), any(CaseEvent.class), any(Claim.class)))
            .thenReturn(claimWithBulkPrintDetails);

        breathingSpaceLetterService.sendLetterToDefendant(ccdCase, claim, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID);

        verify(documentManagementService, once()).uploadDocument(any(String.class), any());

        verify(claimService, once()).saveClaimDocuments(any(String.class), any(), any(), any(ClaimDocumentType.class));

    }

    @Test
    void shouldThrowExceptionWhenDocAssemblyFails() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenThrow(new DocumentGenerationFailedException(new RuntimeException("exception")));

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.breathingSpaceLetter(any(CCDCase.class)))
            .thenReturn(docAssemblyTemplateBody);

        assertThrows(DocumentGenerationFailedException.class,
            () -> breathingSpaceLetterService.sendLetterToDefendant(ccdCase, claim, AUTHORISATION,
                BREATHING_SPACE_LETTER_TEMPLATE_ID));
    }

    @Test
    void shouldCreateAndPreviewLetterFromCCD() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.breathingSpaceLetter(any(CCDCase.class)))
            .thenReturn(docAssemblyTemplateBody);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);

        breathingSpaceLetterService.sendLetterToDefendantFomCCD(ccdCase, claim, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID);

        verify(docAssemblyService, once()).renderTemplate(ccdCase, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID, docAssemblyTemplateBody);
    }

    @Test
    void shouldPublishLetter() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.breathingSpaceLetter(any(CCDCase.class)))
            .thenReturn(docAssemblyTemplateBody);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);

        breathingSpaceLetterService.sendLetterToDefendantFomCCD(ccdCase, claim, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID);

        verify(generalLetterService)
            .publishLetter(any(CCDCase.class), any(Claim.class), anyString(), anyString());

    }

    @Test
    void shouldThrowExceptionWhenDocAssemblyFailsFromCCD() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenThrow(new DocumentGenerationFailedException(new RuntimeException("exception")));

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.breathingSpaceLetter(any(CCDCase.class)))
            .thenReturn(docAssemblyTemplateBody);

        assertThrows(DocumentGenerationFailedException.class,
            () -> breathingSpaceLetterService.sendLetterToDefendantFomCCD(ccdCase, claim, AUTHORISATION,
                BREATHING_SPACE_LETTER_TEMPLATE_ID));
    }

}
