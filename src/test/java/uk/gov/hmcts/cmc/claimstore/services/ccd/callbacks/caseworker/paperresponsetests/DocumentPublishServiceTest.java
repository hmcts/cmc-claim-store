package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperresponsetests;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.mapper.BulkPrintDetailsMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.DocumentPublishService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.PaperResponseLetterService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentPublishServiceTest {
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static final boolean DISABLEN9FORM = true;
    private static final boolean DISABLEN9FORMFALSE = false;
    private static final CCDDocument COVER_LETTER = CCDDocument
        .builder()
        .documentUrl(DOC_URL)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentFileName(DOC_NAME)
        .build();
    private static final CCDDocument OCON9_LETTER = CCDDocument
        .builder()
        .documentUrl(DOC_URL)
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentFileName(DOC_NAME)
        .build();
    private static final CCDDocument OCON_FORM = CCDDocument
        .builder()
        .documentUrl(DOC_URL + "form")
        .documentBinaryUrl(DOC_URL_BINARY)
        .documentFileName(DOC_NAME + "form")
        .build();

    private static final String AUTHORISATION = "auth";
    private static Map<String, Object> VALUES = Collections.emptyMap();
    private static final Document COVER_DOCUMENT = new Document(DOC_URL, VALUES);
    private static final Document OCON_DOCUMENT = new Document(DOC_URL, VALUES);
    private static final Document OCON9_DOCUMENT = new Document(DOC_URL, VALUES);
    @Mock
    private PaperResponseLetterService paperResponseLetterService;
    @Mock
    private PrintableDocumentService printableDocumentService;
    @Mock
    private BulkPrintHandler bulkPrintHandler;

    private DocumentPublishService documentPublishService;

    private BulkPrintDetailsMapper bulkPrintDetailsMapper = new BulkPrintDetailsMapper();

    @BeforeEach
    void setUp() {
        documentPublishService = new DocumentPublishService(
            paperResponseLetterService,
            printableDocumentService,
            bulkPrintHandler,
            bulkPrintDetailsMapper
        );
    }

    @Test
    void shouldPublishDocuments() {
        CCDCase ccdCase = getCcdCase();
        Claim claim = getClaim();

        when(paperResponseLetterService
            .createCoverLetter(eq(ccdCase), eq(AUTHORISATION), eq(DATE.toLocalDate())))
            .thenReturn(COVER_LETTER);
        when(paperResponseLetterService
            .createOconForm(ccdCase, claim, AUTHORISATION, DATE.toLocalDate(), DISABLEN9FORM))
            .thenReturn(OCON_FORM);
        when(printableDocumentService.process(eq(COVER_LETTER), eq(AUTHORISATION))).thenReturn(COVER_DOCUMENT);
        when(printableDocumentService.process(eq(OCON_FORM), eq(AUTHORISATION))).thenReturn(OCON_DOCUMENT);

        when(paperResponseLetterService
            .addCoverLetterToCaseWithDocuments(eq(ccdCase), eq(claim), eq(COVER_LETTER), eq(AUTHORISATION)))
            .thenReturn(ccdCase);

        documentPublishService.publishDocuments(ccdCase,
            claim, AUTHORISATION, DATE.toLocalDate(), true, false);

        verify(paperResponseLetterService).createCoverLetter(eq(ccdCase), eq(AUTHORISATION), eq(DATE.toLocalDate()));
        verify(printableDocumentService).process(eq(COVER_LETTER), eq(AUTHORISATION));
        verify(printableDocumentService).process(eq(OCON_FORM), eq(AUTHORISATION));
        verify(paperResponseLetterService)
            .addCoverLetterToCaseWithDocuments(eq(ccdCase), eq(claim), eq(COVER_LETTER), eq(AUTHORISATION));
    }

    private Claim getClaim() {
        return Claim.builder()
            .claimData(SampleClaimData.builder().build())
            .defendantEmail("email@email.com")
            .defendantId("id")
            .submitterEmail("email@email.com")
            .referenceNumber("ref. number")
            .build();
    }

    @Test
    void shouldPublishN9Documents() {
        CCDCase ccdCase = getCcdCase();

        Claim claim = getClaim();

        when(paperResponseLetterService
            .createCoverLetter(ccdCase, AUTHORISATION, DATE.toLocalDate()))
            .thenReturn(COVER_LETTER);
        when(paperResponseLetterService
            .createOCON9From(ccdCase, AUTHORISATION, DATE.toLocalDate()))
            .thenReturn(OCON9_LETTER);
        when(paperResponseLetterService
            .createOconForm(ccdCase, claim, AUTHORISATION, DATE.toLocalDate(), DISABLEN9FORMFALSE))
            .thenReturn(OCON_FORM);
        when(printableDocumentService.process(OCON_FORM, AUTHORISATION)).thenReturn(OCON_DOCUMENT);
        when(printableDocumentService.process(OCON9_LETTER, AUTHORISATION)).thenReturn(OCON9_DOCUMENT);

        when(paperResponseLetterService
            .addCoverLetterToCaseWithDocuments(ccdCase, claim, COVER_LETTER, AUTHORISATION))
            .thenReturn(ccdCase);

        documentPublishService.publishDocuments(ccdCase, claim, AUTHORISATION, DATE.toLocalDate(), false, true);
        verify(paperResponseLetterService).createOCON9From(ccdCase, AUTHORISATION, DATE.toLocalDate());
        verify(printableDocumentService, times(2)).process(OCON9_LETTER, AUTHORISATION);

    }

    private CCDCase getCcdCase() {
        return CCDCase.builder()
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
    }

}
