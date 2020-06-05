package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseMethod;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.PaperResponseOCON9xFormCallbackHandler.OCON9X_SUBTYPE;

@ExtendWith(MockitoExtension.class)
public class PaperResponseOCON9xFormCallbackHandlerTest {

    public static final String AUTHORISATION = "AUTHORISATION";

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private PaperResponseOCON9xFormCallbackHandler handler;

    private CallbackParams callbackParams;

    @Nested
    class AboutToStartTests {

        @BeforeEach
        void setUp() {
            CallbackRequest callbackRequest =
                CallbackRequest.builder()
                    .eventId(CaseEvent.PAPER_RESPONSE_OCON_9X_FORM.getValue())
                    .caseDetails(CaseDetails.builder().build())
                    .build();

            callbackParams = CallbackParams.builder()
                .type(CallbackType.ABOUT_TO_START)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .request(callbackRequest)
                .build();
        }

        @Nested
        class TypeTests {

            @BeforeEach
            void setUp() {
                var scannedDocuments = Arrays.stream(CCDScannedDocumentType.values())
                    .map(t -> CCDScannedDocument.builder()
                        .type(t)
                        .url(CCDDocument.builder().documentFileName("filename").build())
                        .build()
                    )
                    .map(d -> CCDCollectionElement.<CCDScannedDocument>builder()
                        .value(d).id(UUID.randomUUID().toString())
                        .build())
                    .collect(Collectors.toList());

                when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                    .thenReturn(CCDCase.builder().scannedDocuments(scannedDocuments).build());
            }

            @Test
            void shouldReturnListOfForms() {
                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Set<CCDScannedDocumentType> scannedDocumentTypes =
                    ((List<CCDCollectionElement<CCDScannedDocument>>) response.getData()
                        .get("filteredScannedDocuments"))
                        .stream()
                        .map(CCDCollectionElement::getValue)
                        .map(CCDScannedDocument::getType)
                        .collect(Collectors.toSet());

                assertThat(scannedDocumentTypes).isEqualTo(Set.of(CCDScannedDocumentType.form));
            }

            @ParameterizedTest
            @EnumSource(value = CCDScannedDocumentType.class, mode = EnumSource.Mode.EXCLUDE, names = "form")
            void shouldNotReturnListOfNonForms(CCDScannedDocumentType type) {

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Set<CCDScannedDocumentType> scannedDocumentTypes =
                    ((List<CCDCollectionElement<CCDScannedDocument>>) response.getData()
                        .get("filteredScannedDocuments"))
                        .stream()
                        .map(CCDCollectionElement::getValue)
                        .map(CCDScannedDocument::getType)
                        .collect(Collectors.toSet());

                assertThat(scannedDocumentTypes).doesNotContain(type);
            }
        }

        @Nested
        class SubTypeTests {

            CCDScannedDocument noSubtypeForm;
            CCDScannedDocument subtypeForm;

            @BeforeEach
            void setUp() {
                noSubtypeForm = CCDScannedDocument.builder()
                    .type(CCDScannedDocumentType.form)
                    .url(CCDDocument.builder().documentFileName("filename").build())
                    .build();
                subtypeForm = CCDScannedDocument.builder()
                    .type(CCDScannedDocumentType.form)
                    .url(CCDDocument.builder().documentFileName("filename").build())
                    .subtype("subtype")
                    .build();

                var scannedDocuments = List.of(noSubtypeForm, subtypeForm)
                    .stream()
                    .map(d -> CCDCollectionElement.<CCDScannedDocument>builder()
                        .value(d)
                        .id(UUID.randomUUID().toString())
                        .build())
                    .collect(Collectors.toList());

                when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                    .thenReturn(CCDCase.builder().scannedDocuments(scannedDocuments).build());
            }

            @Test
            void shouldNotReturnFormsWithSubtype() {
                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Set<CCDScannedDocument> scannedDocuments =
                    ((List<CCDCollectionElement<CCDScannedDocument>>) response.getData()
                        .get("filteredScannedDocuments"))
                        .stream()
                        .map(CCDCollectionElement::getValue)
                        .collect(Collectors.toSet());

                assertThat(scannedDocuments).contains(noSubtypeForm);
            }

            @Test
            void shouldReturnFormsWithNoSubtype() {
                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Set<CCDScannedDocument> scannedDocuments =
                    ((List<CCDCollectionElement<CCDScannedDocument>>) response.getData()
                        .get("filteredScannedDocuments"))
                        .stream()
                        .map(CCDCollectionElement::getValue)
                        .collect(Collectors.toSet());

                assertThat(scannedDocuments).doesNotContain(subtypeForm);
            }
        }

        @Nested
        class OCON9XFormTests {

            private final int numberOfForms = 2;
            private List<CCDCollectionElement<CCDScannedDocument>> scannedDocuments;

            @BeforeEach
            void setUp() {
                scannedDocuments = new ArrayList<>();

                for (int i = 1; i <= numberOfForms; i++) {
                    CCDScannedDocument document = CCDScannedDocument.builder()
                        .type(CCDScannedDocumentType.form)
                        .url(CCDDocument.builder().documentFileName("filename" + i).build())
                        .build();
                    CCDCollectionElement<CCDScannedDocument> element =
                        CCDCollectionElement.<CCDScannedDocument>builder()
                        .value(document)
                        .id("id " + i)
                        .build();
                    scannedDocuments.add(element);
                }

                when(caseDetailsConverter.extractCCDCase(any(CaseDetails.class)))
                    .thenReturn(CCDCase.builder().scannedDocuments(scannedDocuments).build());
            }

            @Test
            void shouldReturnDynamicLists() {
                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

                Map<String, Object> ocon9xForm = (Map<String, Object>)response.getData().get("ocon9xForm");

                List<Map<String, String>> listItems = (List<Map<String, String>>) ocon9xForm.get("list_items");

                assertThat(listItems.size()).isEqualTo(numberOfForms);

                assertThat(listItems.stream().map(m -> m.get("code")).collect(Collectors.toSet()))
                    .isEqualTo(scannedDocuments.stream().map(CCDCollectionElement::getId).collect(Collectors.toSet()));

                assertThat(listItems.stream().map(m -> m.get("label")).collect(Collectors.toSet()))
                    .isEqualTo(scannedDocuments.stream()
                        .map(CCDCollectionElement::getValue)
                        .map(CCDScannedDocument::getUrl)
                        .map(CCDDocument::getDocumentFileName)
                        .collect(Collectors.toSet()));

            }
        }
    }

    @Nested
    class MidTests {

        private List<CCDCollectionElement<CCDScannedDocument>> scannedDocuments;

        private CaseDetails caseDetails;

        @BeforeEach
        void setUp() {

            caseDetails = CaseDetails.builder().build();
            CallbackRequest callbackRequest =
                CallbackRequest.builder()
                    .eventId(CaseEvent.PAPER_RESPONSE_OCON_9X_FORM.getValue())
                    .caseDetails(caseDetails)
                    .build();

            callbackParams = CallbackParams.builder()
                .type(CallbackType.MID)
                .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                .request(callbackRequest)
                .build();

            scannedDocuments = IntStream.of(3)
                .mapToObj(i -> CCDScannedDocument.builder()
                    .type(CCDScannedDocumentType.form)
                    .url(CCDDocument.builder().documentFileName("filename" + i).build())
                    .build()
                ).map(d -> CCDCollectionElement.<CCDScannedDocument>builder()
                    .value(d)
                    .id(UUID.randomUUID().toString())
                    .build()
                ).collect(Collectors.toList());

        }

        @Test
        void shouldReturnErrorIfDocumentAdded() {

            var addedScanDocuments = new ArrayList<>(scannedDocuments);
            addedScanDocuments.add(CCDCollectionElement.<CCDScannedDocument>builder()
                .value(CCDScannedDocument.builder().type(CCDScannedDocumentType.form).build())
                .id(UUID.randomUUID().toString())
                .build());
            when(caseDetailsConverter.extractCCDCase(eq(caseDetails)))
                .thenReturn(CCDCase.builder()
                    .scannedDocuments(scannedDocuments)
                    .filteredScannedDocuments(addedScanDocuments)
                    .build());

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            assertThat(response.getErrors())
                .isEqualTo(List.of(PaperResponseOCON9xFormCallbackHandler.SCANNED_DOCUMENTS_MODIFIED_ERROR));
        }

        @Test
        void shouldReturnErrorIfDocumentRemoved() {

            var removedScannedDocuments = new ArrayList<>(scannedDocuments);
            removedScannedDocuments.remove(0);

            when(caseDetailsConverter.extractCCDCase(eq(caseDetails)))
                .thenReturn(CCDCase.builder()
                    .scannedDocuments(scannedDocuments)
                    .filteredScannedDocuments(removedScannedDocuments)
                    .build());

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            assertThat(response.getErrors())
                .isEqualTo(List.of(PaperResponseOCON9xFormCallbackHandler.SCANNED_DOCUMENTS_MODIFIED_ERROR));
        }

        @Test
        void shouldReturnNoErrorIfNoDocumentChanges() {
            when(caseDetailsConverter.extractCCDCase(eq(caseDetails)))
                .thenReturn(CCDCase.builder()
                    .scannedDocuments(scannedDocuments)
                    .filteredScannedDocuments(scannedDocuments)
                    .build());

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);

            assertThat(response.getErrors()).isEqualTo(Collections.emptyList());
        }
    }

    @Nested
    class AboutToSubmit6Tests {

        private LocalDateTime now;

        private final String filename = "filename";
        private final String reference = "reference";

        @Nested
        class Ocon9xSubtypeTests {

            private CCDCase ccdCase;

            @BeforeEach
            void setUp() {
                now = LocalDateTime.now();

                CaseDetails caseDetails = CaseDetails.builder().build();

                CCDScannedDocument document = CCDScannedDocument.builder()
                    .type(CCDScannedDocumentType.form)
                    .deliveryDate(now)
                    .fileName(filename)
                    .build();

                String id = UUID.randomUUID().toString();
                var scannedDocuments = CCDCollectionElement.<CCDScannedDocument>builder()
                    .value(document)
                    .id(id)
                    .build();

                ccdCase = CCDCase.builder()
                    .previousServiceCaseReference(reference)
                    .scannedDocuments(List.of(scannedDocuments))
                    .ocon9xForm(id)
                    .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                        .value(CCDRespondent.builder().build())
                        .build()))
                    .build();

                when(caseDetailsConverter.extractCCDCase(eq(caseDetails))).thenReturn(ccdCase);

                CallbackRequest callbackRequest =
                    CallbackRequest.builder()
                        .eventId(CaseEvent.PAPER_RESPONSE_OCON_9X_FORM.getValue())
                        .caseDetails(CaseDetails.builder().build())
                        .build();

                callbackParams = CallbackParams.builder()
                    .type(CallbackType.ABOUT_TO_SUBMIT)
                    .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
                    .request(callbackRequest)
                    .build();

            }

            @Test
            void shouldUpdateCaseDataCorrectly() {
                handler.handle(callbackParams);

                CCDCollectionElement<CCDRespondent> respondentElement = ccdCase.getRespondents().get(0);

                CCDRespondent respondent = respondentElement
                    .getValue()
                    .toBuilder()
                    .responseSubmittedOn(now)
                    .responseMethod(CCDResponseMethod.OCON_FORM)
                    .build();

                CCDCollectionElement<CCDScannedDocument> scannedDocElement =
                    ccdCase.getScannedDocuments()
                        .get(0);

                CCDScannedDocument scannedDoc = scannedDocElement
                    .getValue()
                    .toBuilder()
                    .fileName(reference + "-scanned-OCON9x-form.pdf")
                    .subtype(OCON9X_SUBTYPE)
                    .deliveryDate(now)
                    .build();

                CCDCase expectedCCDCase = ccdCase.toBuilder()
                    .respondents(List.of(respondentElement.toBuilder().value(respondent).build()))
                    .scannedDocuments(List.of(scannedDocElement.toBuilder().value(scannedDoc).build()))
                    .filteredScannedDocuments(Collections.emptyList())
                    .ocon9xForm(null)
                    .evidenceHandled(CCDYesNoOption.YES)
                    .build();

                verify(caseDetailsConverter).convertToMap(expectedCCDCase);

            }
        }
    }
}
