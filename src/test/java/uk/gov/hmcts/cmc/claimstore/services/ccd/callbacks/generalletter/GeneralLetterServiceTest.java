package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter;

import com.google.common.collect.ImmutableList;
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
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final byte[] PDF_BYTES = new byte[] {1, 2, 3, 4};

    private static final String DRAFT_LETTER_DOC_KEY = "draftLetterDoc";
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
    private ApplicationEventPublisher publisher;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private DocAssemblyResponse docAssemblyResponse;
    @Mock
    private Clock clock;
    @Mock
    private UserService userService;

    private GeneralLetterService generalLetterService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        generalLetterService = new GeneralLetterService(
            docAssemblyService,
            publisher,
            documentManagementService,
            clock,
            userService);
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
            .createGeneralLetter(any(CCDCase.class), anyString(), anyString())).thenReturn(docAssemblyResponse);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
        generalLetterService.createAndPreview(ccdCase, BEARER_TOKEN.name(), GENERAL_LETTER_TEMPLATE_ID);
        verify(docAssemblyService, once()).createGeneralLetter(eq(ccdCase), eq(BEARER_TOKEN.name()),
            eq(GENERAL_LETTER_TEMPLATE_ID));
    }

    @Test
    void shouldThrowExceptionWhenDocAssemblyFails() {
        when(docAssemblyService.createGeneralLetter(any(CCDCase.class), anyString(), anyString()))
            .thenThrow(new DocumentGenerationFailedException(new RuntimeException("exception")));
        assertThrows(DocumentGenerationFailedException.class,
            () -> generalLetterService.createAndPreview(ccdCase, BEARER_TOKEN.name(),
                GENERAL_LETTER_TEMPLATE_ID));
    }

    @Test
    void shouldPrintAndUpdateCaseDocument() throws Exception {
        when(clock.instant()).thenReturn(DATE.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(clock.withZone(LocalDateTimeFactory.UTC_ZONE)).thenReturn(clock);
        doNothing().when(publisher).publishEvent(any(GeneralLetterReadyToPrintEvent.class));
        CCDCase expected = ccdCase.toBuilder()
            .caseDocuments(ImmutableList.<CCDCollectionElement<CCDClaimDocument>>builder()
                .addAll(ccdCase.getCaseDocuments())
                .add(CLAIM_DOCUMENT)
                .build())
            .draftLetterDoc(null)
            .generalLetterContent(null)
            .build();
        when(documentManagementService.downloadDocument(anyString(), any(ClaimDocument.class)))
            .thenReturn(PDF_BYTES);

        CCDCase updatedCase = generalLetterService
            .printAndUpdateCaseDocuments(ccdCase, claim, BEARER_TOKEN.name(), GENERAL_DOCUMENT_NAME);
        verify(documentManagementService, once()).downloadDocument(eq(BEARER_TOKEN.name()), any(ClaimDocument.class));
        assertThat(updatedCase).isEqualTo(expected);
    }

    @Test
    void shouldThrowExceptionWhenPrintAndUpdateCaseDocumentFails() {
        when(docAssemblyService.createGeneralLetter(any(CCDCase.class), anyString(), anyString()))
            .thenThrow(new RuntimeException("exception"));
        assertThrows(RuntimeException.class,
            () -> generalLetterService.createAndPreview(ccdCase, BEARER_TOKEN.name(),
                GENERAL_LETTER_TEMPLATE_ID));
    }
}
