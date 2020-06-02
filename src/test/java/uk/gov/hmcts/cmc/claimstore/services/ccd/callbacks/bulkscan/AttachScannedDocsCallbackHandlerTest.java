package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.bulkscan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDScannedDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseMethod;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.defendant.CCDResponseMethod.OFFLINE;

@ExtendWith(MockitoExtension.class)
class AttachScannedDocsCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private AttachScannedDocsCallbackHandler callbackHandler;

    private CallbackParams callbackParams;

    @BeforeEach
    void setUp() {
        callbackHandler = new AttachScannedDocsCallbackHandler(caseDetailsConverter);

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.ATTACH_SCANNED_DOCS.getValue())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"N9a", "N9b", "N11", "N225", "N180"})
    void shouldSetOfflineResponseMethodForResponseScannedDocuments(String documentCode) {
        CCDCollectionElement<CCDScannedDocument> scannedDocument = CCDCollectionElement.<CCDScannedDocument>builder()
            .value(CCDScannedDocument.builder().subtype(documentCode).build())
            .build();
        CCDCollectionElement<CCDRespondent> respondent = CCDCollectionElement.<CCDRespondent>builder()
            .value(CCDRespondent.builder().build())
            .build();

        CCDCase ccdCase = CCDCase.builder()
            .scannedDocuments(List.of(scannedDocument))
            .respondents(List.of(respondent))
            .build();

        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

        CCDCollectionElement<CCDRespondent> expectedRespondent = CCDCollectionElement.<CCDRespondent>builder()
            .value(CCDRespondent.builder().responseMethod(OFFLINE).build())
            .build();

        CCDCase expectedCcdCase = ccdCase.toBuilder()
            .respondents(List.of(expectedRespondent))
            .build();

        when(caseDetailsConverter.convertToMap(expectedCcdCase)).thenReturn(Collections.emptyMap());

        callbackHandler.handle(callbackParams);

        verify(caseDetailsConverter).convertToMap(expectedCcdCase);

    }

    @ParameterizedTest
    @NullSource
    @EnumSource(value = CCDResponseMethod.class)
    void shouldNotChangeResponseMethodForResponseScannedDocuments(CCDResponseMethod responseMethod) {
        CCDCollectionElement<CCDScannedDocument> scannedDocument = CCDCollectionElement.<CCDScannedDocument>builder()
            .value(CCDScannedDocument.builder().subtype("Non response code").build())
            .build();
        CCDCollectionElement<CCDRespondent> respondent = CCDCollectionElement.<CCDRespondent>builder()
            .value(CCDRespondent.builder().responseMethod(responseMethod).build())
            .build();

        CCDCase ccdCase = CCDCase.builder()
            .scannedDocuments(List.of(scannedDocument))
            .respondents(List.of(respondent))
            .build();

        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(Collections.emptyMap());

        callbackHandler.handle(callbackParams);

        verify(caseDetailsConverter).convertToMap(ccdCase);

    }

    @ParameterizedTest
    @EnumSource(value = CCDClaimDocumentType.class,
        names = {"PAPER_RESPONSE_FULL_ADMIT",
            "PAPER_RESPONSE_PART_ADMIT",
            "PAPER_RESPONSE_STATES_PAID",
            "PAPER_RESPONSE_MORE_TIME",
            "PAPER_RESPONSE_DISPUTES_ALL",
            "PAPER_RESPONSE_COUNTER_CLAIM"})
    void shouldSetOfflineResponseMethodForResponseStaffUploadedDocuments(CCDClaimDocumentType documentType) {
        CCDCollectionElement<CCDClaimDocument> document = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder().documentType(documentType).build())
            .build();
        CCDCollectionElement<CCDRespondent> respondent = CCDCollectionElement.<CCDRespondent>builder()
            .value(CCDRespondent.builder().build())
            .build();

        CCDCase ccdCase = CCDCase.builder()
            .staffUploadedDocuments(List.of(document))
            .respondents(List.of(respondent))
            .build();

        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

        CCDCollectionElement<CCDRespondent> expectedRespondent = CCDCollectionElement.<CCDRespondent>builder()
            .value(CCDRespondent.builder().responseMethod(OFFLINE).build())
            .build();

        CCDCase expectedCcdCase = ccdCase.toBuilder()
            .respondents(List.of(expectedRespondent))
            .build();

        when(caseDetailsConverter.convertToMap(expectedCcdCase)).thenReturn(Collections.emptyMap());

        callbackHandler.handle(callbackParams);

        verify(caseDetailsConverter).convertToMap(expectedCcdCase);

    }

    @ParameterizedTest
    @EnumSource(value = CCDClaimDocumentType.class,
        names = {"PAPER_RESPONSE_FULL_ADMIT",
            "PAPER_RESPONSE_PART_ADMIT",
            "PAPER_RESPONSE_STATES_PAID",
            "PAPER_RESPONSE_MORE_TIME",
            "PAPER_RESPONSE_DISPUTES_ALL",
            "PAPER_RESPONSE_COUNTER_CLAIM"},
        mode = EnumSource.Mode.EXCLUDE)
    void shouldNotChangeResponseMethodForResponseStaffUploadedDocuments(CCDClaimDocumentType documentType) {
        CCDCollectionElement<CCDClaimDocument> document = CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder().documentType(documentType).build())
            .build();
        CCDCollectionElement<CCDRespondent> respondent = CCDCollectionElement.<CCDRespondent>builder()
            .value(CCDRespondent.builder().build())
            .build();

        CCDCase ccdCase = CCDCase.builder()
            .staffUploadedDocuments(List.of(document))
            .respondents(List.of(respondent))
            .build();

        when(caseDetailsConverter.extractCCDCase(any())).thenReturn(ccdCase);

        when(caseDetailsConverter.convertToMap(ccdCase)).thenReturn(Collections.emptyMap());

        callbackHandler.handle(callbackParams);

        verify(caseDetailsConverter).convertToMap(ccdCase);

    }
}

