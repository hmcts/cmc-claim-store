package uk.gov.hmcts.cmc.claimstore.generalletter;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.generalletter.GeneralLetterService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.docassembly.domain.DocAssemblyResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@ExtendWith(MockitoExtension.class)
class GeneralLetterServiceTest {

    private CallbackRequest callbackRequest;
    private CallbackParams callbackParams;
    private Map<String, Object> data;
    private static final String LETTER_CONTENT = "letterContent";
    private static final String CHANGE_CONTACT_PARTY = "changeContactParty";
    private static final CCDCase ccdCase = CCDCase.builder().build();
    private static final String DOC_URL = "http://success.test";
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

    private GeneralLetterService generalLetterService;

    @BeforeEach
    void setUp() {
        generalLetterService = new GeneralLetterService(caseDetailsConverter,
            docAssemblyService,
            publisher,
            documentManagementService);
        data = new HashMap<>();
        data.put(CHANGE_CONTACT_PARTY, "claimant");
        when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
        when(docAssemblyService
            .createGeneralLetter(any(CCDCase.class), anyString(), anyMap())).thenReturn(docAssemblyResponse);
        when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
    }

    @Nested
    @DisplayName("Create and review general letter")
    class CreateAndPreviewLetter {
        @Test
        void shouldCreateAndPreviewLetterWhenBodyNotNull() {
            data.put(LETTER_CONTENT, "content");
            caseDetails = CaseDetails.builder()
                .data(data)
                .build();
            callbackRequest = CallbackRequest
                .builder()
                .caseDetails(caseDetails)
                .build();
            callbackParams = CallbackParams.builder()
                .request(callbackRequest)
                .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
                .build();

            generalLetterService.createAndPreview(callbackParams);
            verify(caseDetailsConverter, once()).extractCCDCase(eq(caseDetails));
            verify(docAssemblyService, once()).createGeneralLetter(eq(ccdCase), eq(BEARER_TOKEN.name()), eq(data));
        }

        @Test
        void shouldNotCreateAndPreviewLetterWhenBodyIsNull() {
            caseDetails = CaseDetails.builder()
                .data(data)
                .build();
            callbackRequest = CallbackRequest
                .builder()
                .caseDetails(caseDetails)
                .build();
            callbackParams = CallbackParams.builder()
                .request(callbackRequest)
                .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
                .build();
            when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class))).thenReturn(ccdCase);
            when(docAssemblyService
                .createGeneralLetter(any(CCDCase.class), anyString(), anyMap())).thenReturn(docAssemblyResponse);
            when(docAssemblyResponse.getRenditionOutputLocation()).thenReturn(DOC_URL);
            generalLetterService.createAndPreview(callbackParams);
            verify(caseDetailsConverter, once()).extractCCDCase(eq(caseDetails));
            verify(docAssemblyService, never()).createGeneralLetter(eq(ccdCase), eq(BEARER_TOKEN.name()), eq(data));
        }
    }
}
