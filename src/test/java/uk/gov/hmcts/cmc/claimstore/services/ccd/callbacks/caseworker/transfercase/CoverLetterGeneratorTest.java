package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_COURT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.transfercase.NoticeOfTransferLetterType.FOR_DEFENDANT;

@ExtendWith(MockitoExtension.class)
public class CoverLetterGeneratorTest {

    private CoverLetterGenerator coverLetterGenerator;
    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private NoticeOfTransferLetterTemplateMapper noticeOfTransferLetterTemplateMapper;

    private CCDCase ccdCase;

    private DocAssemblyTemplateBody formPayloadForCourt;

    private CCDDocument generatedCoverDoc = CCDDocument.builder().build();

    private static final String AUTHORISATION = "Bearer:auth_token";
    private static final String COURT_LETTER_TEMPLATE_ID = "courtLetterTemplateId";
    private static final String DEFENDANT_LETTER_TEMPLATE_ID = "defendantLetterTemplateId";
    private CCDDocument coverLetterActual;

    @BeforeEach
    public void beforeEach() {
        coverLetterGenerator = new CoverLetterGenerator(docAssemblyService, noticeOfTransferLetterTemplateMapper);
        ccdCase = SampleData.getCCDLegalCase();
        when(noticeOfTransferLetterTemplateMapper.noticeOfTransferLetterBodyForCourt(ccdCase, AUTHORISATION))
            .thenReturn(formPayloadForCourt);
    }

    @Test
    void shouldGenerateCoverLetterForCourt() {
        coverLetterActual = new CCDDocument(
            null,
            null,
            "ref no-notice-of-transfer-for-court.pdf");
        when(docAssemblyService.generateDocument(ccdCase, AUTHORISATION, formPayloadForCourt, COURT_LETTER_TEMPLATE_ID))
            .thenReturn(generatedCoverDoc);
        CCDDocument coverLetter = coverLetterGenerator
            .generate(ccdCase, AUTHORISATION, FOR_COURT, COURT_LETTER_TEMPLATE_ID);
        verify(noticeOfTransferLetterTemplateMapper)
            .noticeOfTransferLetterBodyForCourt(ccdCase, AUTHORISATION);
        verify(docAssemblyService)
            .generateDocument(ccdCase, AUTHORISATION, formPayloadForCourt, COURT_LETTER_TEMPLATE_ID);
        assertEquals(coverLetterActual, coverLetter);
    }

    @Test
    void shouldGenerateCoverLetterForDefendant() {
        coverLetterActual = new CCDDocument(
            null,
            null,
            "ref no-notice-of-transfer-for-defendant.pdf");
        when(docAssemblyService.generateDocument(
            ccdCase,
            AUTHORISATION,
            formPayloadForCourt,
            DEFENDANT_LETTER_TEMPLATE_ID))
            .thenReturn(generatedCoverDoc);
        CCDDocument coverLetter = coverLetterGenerator
            .generate(ccdCase, AUTHORISATION, FOR_DEFENDANT, DEFENDANT_LETTER_TEMPLATE_ID);
        verify(noticeOfTransferLetterTemplateMapper)
            .noticeOfTransferLetterBodyForCourt(ccdCase, AUTHORISATION);
        verify(docAssemblyService)
            .generateDocument(ccdCase, AUTHORISATION, formPayloadForCourt, DEFENDANT_LETTER_TEMPLATE_ID);
        assertEquals(coverLetterActual, coverLetter);
    }
}
