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
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace.BreathingSpaceLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBodyMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;
import uk.gov.hmcts.reform.docassembly.exception.DocumentGenerationFailedException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@ExtendWith(MockitoExtension.class)
class BreathingSpaceLetterServiceTest {

    public static final String BREATHING_SPACE_LETTER_TEMPLATE_ID = "breathingSpaceEnteredTemplateID";
    private static final String DOC_URL = "http://success.test";
    private static final Claim claim = SampleClaim.builder().build();
    private CCDCase ccdCase;

    @Mock
    private DocAssemblyService docAssemblyService;
    @Mock
    private GeneralLetterService generalLetterService;
    @Mock
    private DocAssemblyTemplateBodyMapper docAssemblyTemplateBodyMapper;
    @Mock
    private DocAssemblyResponse docAssemblyResponse;

    private BreathingSpaceLetterService breathingSpaceLetterService;

    @BeforeEach
    void setUp() {
        breathingSpaceLetterService = new BreathingSpaceLetterService(
            generalLetterService, docAssemblyService,
            docAssemblyTemplateBodyMapper);

        ccdCase = CCDCase.builder()
            .previousServiceCaseReference("000MC001")
            .caseDocuments(ImmutableList.of(CCDCollectionElement.<CCDClaimDocument>builder()
                .value(CCDClaimDocument.builder()
                    .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                    .documentName("000MC001-breathing-space-entered.pdf")
                    .build())
                .build()))
            .build();
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

        breathingSpaceLetterService.sendLetterToDefendant(ccdCase, claim, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID);

        verify(docAssemblyService, once()).renderTemplate(eq(ccdCase), eq(BEARER_TOKEN.name()),
            eq(BREATHING_SPACE_LETTER_TEMPLATE_ID), eq(docAssemblyTemplateBody));
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

        breathingSpaceLetterService.sendLetterToDefendant(ccdCase, claim, BEARER_TOKEN.name(),
            BREATHING_SPACE_LETTER_TEMPLATE_ID);

        verify(generalLetterService)
            .publishLetter(any(CCDCase.class), any(Claim.class), anyString(), anyString());

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
