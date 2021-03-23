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
import uk.gov.hmcts.cmc.claimstore.documents.PrintService;
import uk.gov.hmcts.cmc.claimstore.services.ClaimService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
class BreathingSpaceLetterServiceTest {

    public static final String BREATHING_SPACE_LETTER_TEMPLATE_ID = "breathingSpaceEnteredTemplateID";
    public static final String AUTHORISATION = "BEARER_TOKEN";
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final CCDDocument DRAFT_LETTER_DOC = CCDDocument.builder()
        .documentFileName(DOC_NAME)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentUrl(DOC_URL).build();
    private static final String DOCUMENT_URL = null;
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "000MC001-breathing-space-entered";
    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_BINARY_URL)
        .documentFileName(DOCUMENT_FILE_NAME)
        .build();
    private static final Claim claim = SampleClaim
        .builder()
        .build();
    private static final URI DOCUMENT_URI = URI.create("http://localhost/doc.pdf");

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
    private BreathingSpaceLetterService breathingSpaceLetterService;

    @BeforeEach
    void setUp() {
        breathingSpaceLetterService = new BreathingSpaceLetterService(
            docAssemblyService,
            docAssemblyTemplateBodyMapper, printableDocumentService, bulkPrintService, claimService,
            documentManagementService);

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
    void shouldThrowExceptionWhenDocAssemblyFails() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenThrow(new DocumentGenerationFailedException(new RuntimeException("exception")));

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.breathingSpaceLetter(any(CCDCase.class)))
            .thenReturn(docAssemblyTemplateBody);

        assertThrows(DocumentGenerationFailedException.class,
            () -> breathingSpaceLetterService.sendLetterToDefendant(ccdCase, claim, BEARER_TOKEN.name(),
                BREATHING_SPACE_LETTER_TEMPLATE_ID));
    }

}
