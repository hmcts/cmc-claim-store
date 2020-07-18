package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDBulkPrintDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.BulkPrintDetailsMapper;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;
import uk.gov.hmcts.reform.document.domain.Document;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;
import static uk.gov.hmcts.cmc.domain.models.bulkprint.PrintRequestType.GENERAL_LETTER;
import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.GENERAL_LETTER_PDF;

@ExtendWith(MockitoExtension.class)
class GeneralLetterServiceTest {

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
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};

    public static final String GENERAL_LETTER_TEMPLATE_ID = "generalLetterTemplateId";
    public static final String GENERAL_DOCUMENT_NAME = "document-name";

    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOC_URL)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentFileName(GENERAL_DOCUMENT_NAME)
        .build();
    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentName(GENERAL_DOCUMENT_NAME)
                .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                .build())
            .build();
    private CCDCase ccdCase;

    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocAssemblyResponse docAssemblyResponse;
    @Mock
    private Clock clock;
    @Mock
    private UserService userService;
    @Mock
    private BulkPrintHandler bulkPrintHandler;
    @Mock
    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;

    private GeneralLetterService generalLetterService;
    private UserDetails userDetails;
    private BulkPrintDetailsMapper bulkPrintDetailsMapper = new BulkPrintDetailsMapper();

    @BeforeEach
    void setUp() {
        generalLetterService = new GeneralLetterService(
            docAssemblyService,
            bulkPrintHandler,
            new PrintableDocumentService(documentManagementService),
            clock,
            userService,
            docAssemblyTemplateBodyMapper,
            documentManagementService,
            bulkPrintDetailsMapper);

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

        userDetails = SampleUserDetails.builder()
            .withForename("Judge")
            .withSurname("McJudge")
            .build();
    }

    @Test
    void shouldPrepopulate() {
        when(userService.getUserDetails(eq(BEARER_TOKEN.name()))).thenReturn(userDetails);
        generalLetterService.prepopulateData(BEARER_TOKEN.name());
        verify(userService, once()).getUserDetails(eq(BEARER_TOKEN.name()));
    }

    @Test
    void shouldCreateAndPreviewLetter() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenReturn(docAssemblyResponse);

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.generalLetterBody(any(CCDCase.class))).thenReturn(docAssemblyTemplateBody);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);

        generalLetterService.generateLetter(ccdCase, BEARER_TOKEN.name(), GENERAL_LETTER_TEMPLATE_ID);

        verify(docAssemblyService, once()).renderTemplate(eq(ccdCase), eq(BEARER_TOKEN.name()),
            eq(GENERAL_LETTER_TEMPLATE_ID), eq(docAssemblyTemplateBody));
    }

    @Test
    void shouldThrowExceptionWhenDocAssemblyFails() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenThrow(new DocumentGenerationFailedException(new RuntimeException("exception")));

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.generalLetterBody(any(CCDCase.class))).thenReturn(docAssemblyTemplateBody);

        assertThrows(DocumentGenerationFailedException.class,
            () -> generalLetterService.generateLetter(ccdCase, BEARER_TOKEN.name(),
                GENERAL_LETTER_TEMPLATE_ID));
    }

    @Test
    void shouldPrintAndUpdateCaseDocument() {
        when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);
        BulkPrintDetails bulkPrintDetails = BulkPrintDetails.builder().printRequestType(GENERAL_LETTER)
            .printRequestId("requestId")
            .build();

        when(bulkPrintHandler
            .printGeneralLetter(eq(claim), any(uk.gov.hmcts.reform.sendletter.api.Document.class), anyString()))
            .thenReturn(bulkPrintDetails);
        CCDCase expected = ccdCase.toBuilder()
            .caseDocuments(ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
                .addAll(ccdCase.getCaseDocuments())
                .add(CLAIM_DOCUMENT)
                .build())
            .bulkPrintDetails(ImmutableList.<CCDCollectionElement<CCDBulkPrintDetails>>builder()
                .addAll(ccdCase.getBulkPrintDetails())
                .add(bulkPrintDetailsMapper.to(bulkPrintDetails))
                .build()
            )
            .draftLetterDoc(null)
            .contactChangeParty(null)
            .contactChangeContent(null)
            .generalLetterContent(null)
            .build();
        when(documentManagementService.downloadDocument(anyString(), any(ClaimDocument.class)))
            .thenReturn(PDF_BYTES);

        when(documentManagementService.getDocumentMetaData(anyString(), anyString()))
            .thenReturn(getLinks());

        CCDCase updatedCase = generalLetterService
            .publishLetter(ccdCase, claim, BEARER_TOKEN.name(), GENERAL_DOCUMENT_NAME);

        verify(documentManagementService, once()).downloadDocument(eq(BEARER_TOKEN.name()), any(ClaimDocument.class));
        assertThat(updatedCase).isEqualTo(expected);
    }

    @NotNull
    private Document getLinks() {
        Document document = new Document();
        Document.Links links = new Document.Links();
        links.binary = new Document.Link();
        links.binary.href = DOC_URL_BINARY;
        document.links = links;
        return document;
    }

    @Test
    void shouldThrowExceptionWhenPrintAndUpdateCaseDocumentFails() {
        when(docAssemblyService
            .renderTemplate(any(CCDCase.class), anyString(), anyString(), any(DocAssemblyTemplateBody.class)))
            .thenThrow(new RuntimeException("exception"));

        DocAssemblyTemplateBody docAssemblyTemplateBody = DocAssemblyTemplateBody.builder().build();
        when(docAssemblyTemplateBodyMapper.generalLetterBody(any(CCDCase.class))).thenReturn(docAssemblyTemplateBody);

        assertThrows(RuntimeException.class,
            () -> generalLetterService.generateLetter(ccdCase, BEARER_TOKEN.name(),
                GENERAL_LETTER_TEMPLATE_ID));
    }
}
