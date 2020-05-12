package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableList;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.CORRESPONDENCE;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.MEDIATION_AGREEMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType.OTHER;

@ExtendWith(MockitoExtension.class)
class ManageDocumentsCallbackHandlerTest {
    private static final String PAPER_RESPONSE_PREFIX = "PAPER_RESPONSE";

    private static final Set<CCDClaimDocumentType> NON_PAPER_RESPONSE_TYPES
        = Set.of(CORRESPONDENCE, MEDIATION_AGREEMENT, OTHER);

    private static final Set<CCDClaimDocumentType> PAPER_RESPONSE_TYPES
        = Arrays.stream(CCDClaimDocumentType.values())
        .filter(t -> t.name().startsWith(PAPER_RESPONSE_PREFIX))
        .collect(Collectors.toSet());

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

            CCDCollectionElement<CCDClaimDocument> element =
                buildCCDCollection(CCDClaimDocumentType.values()[0], null);

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            CCDCase ccdCasBefore = CCDCase.builder()
                .build();

            when(caseDetailsConverter.extractCCDCase(eq(caseDetails))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractCCDCase(eq(caseDetailsBefore))).thenReturn(ccdCasBefore);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNull(response.getErrors());
        }

        @Nested
        @DisplayName("No differences test")
        class NoDifferencesTest {
            private CaseDetails caseDetails;
            private CaseDetails caseDetailsBefore;
            private CallbackParams callbackParams;

            @BeforeEach
            void setUp() {

                caseDetails = CaseDetails.builder().build();
                caseDetailsBefore = CaseDetails.builder()
                    .data(ImmutableMap.of("different", "data"))
                    .build();

                CallbackRequest request = CallbackRequest.builder()
                    .caseDetails(caseDetails)
                    .caseDetailsBefore(caseDetailsBefore)
                    .build();

                callbackParams = CallbackParams.builder()
                    .type(CallbackType.MID)
                    .request(request)
                    .build();

            }

            @Test
            void shouldReturnErrorsIfThereAreNoDifferencesNullStaffDocuments() {

                CCDCase ccdCase = CCDCase.builder().build();

                when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
                when(caseDetailsConverter.extractCCDCase(caseDetailsBefore)).thenReturn(ccdCase);

                AboutToStartOrSubmitCallbackResponse response
                    = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Assertions.assertNotNull(response.getErrors());
                assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.NO_CHANGES_ERROR_MESSAGE));
            }

            @Test
            void shouldReturnErrorsIfThereAreNoDifferencesNullBefore() {

                CCDCase ccdCase = CCDCase.builder()
                    .build();

                CCDCase ccdCaseBefore = CCDCase.builder()
                    .staffUploadedDocuments(List.of())
                    .build();

                when(caseDetailsConverter.extractCCDCase(eq(caseDetails))).thenReturn(ccdCase);
                when(caseDetailsConverter.extractCCDCase(eq(caseDetailsBefore))).thenReturn(ccdCaseBefore);

                AboutToStartOrSubmitCallbackResponse response
                    = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Assertions.assertNotNull(response.getErrors());
                assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.NO_CHANGES_ERROR_MESSAGE));
            }

            @Test
            void shouldReturnErrorsIfThereAreNoDifferencesNullAfter() {
                CCDCase ccdCase = CCDCase.builder()
                    .staffUploadedDocuments(List.of())
                    .build();

                CCDCase ccdCaseBefore = CCDCase.builder()
                    .build();

                when(caseDetailsConverter.extractCCDCase(eq(caseDetails))).thenReturn(ccdCase);
                when(caseDetailsConverter.extractCCDCase(eq(caseDetailsBefore))).thenReturn(ccdCaseBefore);

                AboutToStartOrSubmitCallbackResponse response
                    = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Assertions.assertNotNull(response.getErrors());
                assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.NO_CHANGES_ERROR_MESSAGE));
            }

            @Test
            void shouldReturnErrorsIfThereAreNoDifferencesEmptyStaffUploadedDocuments() {

                CCDCase ccdCase = CCDCase.builder()
                    .staffUploadedDocuments(List.of())
                    .build();

                when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
                when(caseDetailsConverter.extractCCDCase(caseDetailsBefore)).thenReturn(ccdCase);

                AboutToStartOrSubmitCallbackResponse response
                    = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Assertions.assertNotNull(response.getErrors());
                assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.NO_CHANGES_ERROR_MESSAGE));
            }
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

        @ParameterizedTest(name
            = "#{index} - shouldNotReturnErrorsIfNoPaperResponseSelected={arguments}")
        @ArgumentsSource(NonPaperResponseType.class)
        void shouldNotReturnErrorsIfNoPaperResponseSelected(CCDClaimDocumentType paperResponseType) {
            CCDCollectionElement<CCDClaimDocument> element =
                buildCCDCollection(paperResponseType, null);

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            CCDCase ccdCasBefore = CCDCase.builder()
                .build();

            when(caseDetailsConverter.extractCCDCase(eq(caseDetails))).thenReturn(ccdCase);
            when(caseDetailsConverter.extractCCDCase(eq(caseDetailsBefore))).thenReturn(ccdCasBefore);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNull(response.getErrors());
        }

        @ParameterizedTest(name
            = "#{index} - shouldReturnErrorIfPaperResponseSelected={arguments}")
        @ArgumentsSource(PaperResponseTypeProvider.class)
        void shouldReturnErrorIfPaperResponseSelected(CCDClaimDocumentType paperResponseType) {
            CCDCollectionElement<CCDClaimDocument> element =
                buildCCDCollection(paperResponseType, null);

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(ImmutableList.of(element))
                .build();

            when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
            when(caseDetailsConverter.extractCCDCase(caseDetailsBefore)).thenReturn(CCDCase.builder().build());

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNotNull(response.getErrors());
            assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.PAPER_RESPONSE_ERROR_MESSAGE));
        }

        @ParameterizedTest
        @ArgumentsSource(PaperResponseTypeProvider.class)
        void shouldNotReturnErrorIfExistingPaperResponse(CCDClaimDocumentType paperResponseType) {
            List<CCDClaimDocumentType> uploadedDocumentTypes = new ArrayList(NON_PAPER_RESPONSE_TYPES);

            uploadedDocumentTypes.add(paperResponseType);

            String paperResponseId = RandomString.make();

            var staffUploadedDocuments = uploadedDocumentTypes.stream()
                .map(t -> buildCCDCollection(t, t == paperResponseType ? paperResponseId : null))
                .collect(Collectors.toList());

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(staffUploadedDocuments)
                .build();

            CCDCase ccdCaseBefore = CCDCase.builder()
                .staffUploadedDocuments(List.of(buildCCDCollection(paperResponseType, paperResponseId)))
                .build();

            when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
            when(caseDetailsConverter.extractCCDCase(caseDetailsBefore)).thenReturn(ccdCaseBefore);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNull(response.getErrors());
        }

        @ParameterizedTest(name
            = "#{index} - shouldReturnErrorIfModifyingTypeOfExistingPaperResponseToDifferentPaperResponse={arguments}")
        @ArgumentsSource(PaperResponseToPaperResponseType.class)
        void shouldReturnErrorIfModifyingTypeOfExistingPaperResponseToDifferentPaperResponse(
            CCDClaimDocumentType paperResponseType,
            CCDClaimDocumentType changedType) {
            String paperResponseId = RandomString.make();

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(List.of(buildCCDCollection(changedType, paperResponseId)))
                .build();

            CCDCase ccdCaseBefore = CCDCase.builder()
                .staffUploadedDocuments(List.of(buildCCDCollection(paperResponseType, paperResponseId)))
                .build();

            when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
            when(caseDetailsConverter.extractCCDCase(caseDetailsBefore)).thenReturn(ccdCaseBefore);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNotNull(response.getErrors());
            assertThat(response.getErrors(), contains(ManageDocumentsCallbackHandler.PAPER_RESPONSE_ERROR_MESSAGE));
        }

        @ParameterizedTest(name
            = "#{index} - shouldNotReturnErrorIfModifyingTypeOfExistingPaperResponseToNonPaperResponse={arguments}")
        @ArgumentsSource(PaperResponseToNonPaperResponseType.class)
        void shouldNotReturnErrorIfModifyingTypeOfExistingPaperResponseToNonPaperResponse(
            CCDClaimDocumentType paperResponseType,
            CCDClaimDocumentType changedType) {
            String paperResponseId = RandomString.make();

            CCDCase ccdCase = CCDCase.builder()
                .staffUploadedDocuments(List.of(buildCCDCollection(changedType, paperResponseId)))
                .build();

            CCDCase ccdCaseBefore = CCDCase.builder()
                .staffUploadedDocuments(List.of(buildCCDCollection(paperResponseType, paperResponseId)))
                .build();

            when(caseDetailsConverter.extractCCDCase(caseDetails)).thenReturn(ccdCase);
            when(caseDetailsConverter.extractCCDCase(caseDetailsBefore)).thenReturn(ccdCaseBefore);

            CallbackRequest request = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();

            CallbackParams callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .request(request)
                .build();

            AboutToStartOrSubmitCallbackResponse response
                = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            Assertions.assertNull(response.getErrors());
        }
    }

    static class NonPaperResponseType implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            Stream.Builder<Arguments> argumentBuilder = Stream.builder();

            for (CCDClaimDocumentType type : NON_PAPER_RESPONSE_TYPES) {
                argumentBuilder.add(Arguments.of(type));
            }

            return argumentBuilder.build();
        }
    }

    static class PaperResponseTypeProvider implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            Stream.Builder<Arguments> argumentBuilder = Stream.builder();
            for (CCDClaimDocumentType documentType : PAPER_RESPONSE_TYPES) {
                argumentBuilder.add(Arguments.of(documentType));
            }
            return argumentBuilder.build();
        }
    }

    static class PaperResponseToPaperResponseType implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            Stream.Builder<Arguments> argumentBuilder = Stream.builder();
            for (CCDClaimDocumentType beforeType : PAPER_RESPONSE_TYPES) {

                for (CCDClaimDocumentType changedType : PAPER_RESPONSE_TYPES) {
                    if (beforeType ==  changedType) {
                        continue;
                    }

                    argumentBuilder.add(Arguments.of(beforeType, changedType));
                }
            }
            return argumentBuilder.build();
        }
    }

    static class PaperResponseToNonPaperResponseType implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            Stream.Builder<Arguments> argumentBuilder = Stream.builder();
            for (CCDClaimDocumentType beforeType : PAPER_RESPONSE_TYPES) {

                for (CCDClaimDocumentType changedType : NON_PAPER_RESPONSE_TYPES) {
                    if (beforeType ==  changedType) {
                        continue;
                    }

                    argumentBuilder.add(Arguments.of(beforeType, changedType));
                }
            }
            return argumentBuilder.build();
        }
    }

    private CCDCollectionElement<CCDClaimDocument> buildCCDCollection(CCDClaimDocumentType paperResponseType,
                                                                      String id) {
        CCDClaimDocument claimDocument =
            CCDClaimDocument.builder()
                .documentType(paperResponseType)
                .build();

        return CCDCollectionElement.<CCDClaimDocument>builder()
            .value(claimDocument)
            .id(id == null ? RandomString.make() : id)
            .build();
    }
}
