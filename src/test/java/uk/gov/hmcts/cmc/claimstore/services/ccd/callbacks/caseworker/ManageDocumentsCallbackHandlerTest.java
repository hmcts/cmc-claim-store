package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageDocumentsCallbackHandlerTest {

    private ManageDocumentsCallbackHandler handler;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @BeforeEach
    void setUp() {
        handler = new ManageDocumentsCallbackHandler(caseDetailsConverter);
    }

    @Nested
    @DisplayName("Modifications test")
    class ModificationsTests {

        @BeforeEach
        void setUp() {
            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(CCDCase.builder().build());
        }

        @Test
        void shouldNotReturnErrorsIfThereAreDifferences() {
            CaseDetails caseDetails = CaseDetails.builder().build();
            CaseDetails caseDetailsBefore = CaseDetails.builder()
                .data(ImmutableMap.of("different", "data"))
                .build();

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNull(response.getErrors());
        }

        @Test
        void shouldReturnErrorsIfThereAreNoDifferences() {
            CaseDetails caseDetails = CaseDetails.builder().build();

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetails)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNotNull(response.getErrors());
            assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.NO_CHANGES_ERROR_MESSAGE));
        }
    }

    @Nested
    @DisplayName("Paper Response test")
    class PaperResponseTests {
        private CaseDetails caseDetails;
        private CaseDetails caseDetailsBefore;

        @BeforeEach
        void setUp() {

            caseDetails = CaseDetails.builder().build();
            caseDetailsBefore = CaseDetails.builder()
                .data(ImmutableMap.of("different", "data"))
                .build();
        }

        @Test
        void shouldNotReturnErrorsIfNoPaperResponseSelected() {
            CCDClaimDocument claimDocument =
                CCDClaimDocument.builder()
                    .documentType(CCDClaimDocumentType.MEDIATION_AGREEMENT)
                    .build();

            CCDCollectionElement<CCDClaimDocument> element = CCDCollectionElement.<CCDClaimDocument>builder()
                .value(claimDocument)
                .build();

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNull(response.getErrors());
        }

        @Test
        void shouldReturnErrorIfPaperResponseDisputesAllSelected() {
            CCDClaimDocument claimDocument =
                CCDClaimDocument.builder()
                    .documentType(CCDClaimDocumentType.PAPER_RESPONSE_DISPUTES_ALL)
                    .build();

            CCDCollectionElement<CCDClaimDocument> element = CCDCollectionElement.<CCDClaimDocument>builder()
                .value(claimDocument)
                .build();

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNotNull(response.getErrors());
            assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.PAPER_RESPONSE_ERROR_MESSAGE));
        }

        @Test
        void shouldReturnErrorIfPaperResponseFullAdmitSelected() {
            CCDClaimDocument claimDocument =
                CCDClaimDocument.builder()
                    .documentType(CCDClaimDocumentType.PAPER_RESPONSE_FULL_ADMIT)
                    .build();

            CCDCollectionElement<CCDClaimDocument> element = CCDCollectionElement.<CCDClaimDocument>builder()
                .value(claimDocument)
                .build();

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNotNull(response.getErrors());
            assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.PAPER_RESPONSE_ERROR_MESSAGE));
        }

        @Test
        void shouldReturnErrorIfPaperResponseMoreTimeSelected() {
            CCDClaimDocument claimDocument =
                CCDClaimDocument.builder()
                    .documentType(CCDClaimDocumentType.PAPER_RESPONSE_MORE_TIME)
                    .build();

            CCDCollectionElement<CCDClaimDocument> element = CCDCollectionElement.<CCDClaimDocument>builder()
                .value(claimDocument)
                .build();

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNotNull(response.getErrors());
            assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.PAPER_RESPONSE_ERROR_MESSAGE));
        }

        @Test
        void shouldReturnErrorIfPaperResponsePartAdmitSelected() {
            CCDClaimDocument claimDocument =
                CCDClaimDocument.builder()
                    .documentType(CCDClaimDocumentType.PAPER_RESPONSE_PART_ADMIT)
                    .build();

            CCDCollectionElement<CCDClaimDocument> element = CCDCollectionElement.<CCDClaimDocument>builder()
                .value(claimDocument)
                .build();

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNotNull(response.getErrors());
            assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.PAPER_RESPONSE_ERROR_MESSAGE));
        }

        @Test
        void shouldReturnErrorIfPaperResponseStatesPaidSelected() {
            CCDClaimDocument claimDocument =
                CCDClaimDocument.builder()
                    .documentType(CCDClaimDocumentType.PAPER_RESPONSE_STATES_PAID)
                    .build();

            CCDCollectionElement<CCDClaimDocument> element = CCDCollectionElement.<CCDClaimDocument>builder()
                .value(claimDocument)
                .build();

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse)handler.handle(callbackParams);

            Assertions.assertNotNull(response.getErrors());
            assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.PAPER_RESPONSE_ERROR_MESSAGE));
        }

    }
}
