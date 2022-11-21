package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrintableDocumentServiceTest {

    private static final String AUTHORISATION = "Bearer: aaaa";
    CCDDocument document = CCDDocument.builder().documentBinaryUrl("http://www.cnn.com")
        .documentFileName("fileName")
        .documentUrl("http://www.cnn.com").build();
    private PrintableDocumentService printableDocumentService;
    @Mock
    private DocumentManagementService<uk.gov.hmcts.reform
        .document.domain.Document> legacyDocumentManagementService;
    @Mock
    private DocumentManagementService<uk.gov.hmcts.reform
        .ccd.document.am.model.Document> securedDocumentManagementService;

    @BeforeEach
    void setUp() {
        printableDocumentService = new PrintableDocumentService(securedDocumentManagementService, legacyDocumentManagementService, false);
        setLegacyDocumentManagementService(legacyDocumentManagementService);
        setSecuredDocumentManagementService(securedDocumentManagementService);
    }

    @Test
    void shouldDownloadDocumentFromDocumentManagement() {

        when(legacyDocumentManagementService.downloadDocument(anyString(), any(ClaimDocument.class)))
            .thenReturn(new byte[]{1, 2, 3, 4});

        printableDocumentService.pdf(document, AUTHORISATION);

        verify(legacyDocumentManagementService, times(1)).downloadDocument(
            anyString(),
            any(ClaimDocument.class));
    }

    @Test
    void shouldThrowExceptionIfDownloadUrlIsWrong() {

        when(legacyDocumentManagementService.downloadDocument(
            anyString(),
            any(ClaimDocument.class))).thenThrow(new IllegalArgumentException("Exception"));

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> printableDocumentService.pdf(document, AUTHORISATION));

    }

    @Test
    void shouldThrowExceptionIfUrlIsWrong() {

        when(legacyDocumentManagementService.downloadDocument(
            anyString(),
            any(ClaimDocument.class))).thenThrow(new IllegalArgumentException("Exception"));

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> printableDocumentService.process(document, AUTHORISATION));

    }

    @Autowired
    @Qualifier("legacyDocumentManagementService")
    private void setLegacyDocumentManagementService(DocumentManagementService<uk.gov.hmcts.reform
        .document.domain.Document> documentManagementService) {
        this.legacyDocumentManagementService = documentManagementService;
    }

    @Autowired
    @Qualifier("securedDocumentManagementService")
    private void setSecuredDocumentManagementService(DocumentManagementService<uk.gov.hmcts.reform
        .ccd.document.am.model.Document> securedDocumentManagementService) {
        this.securedDocumentManagementService = securedDocumentManagementService;

    }
}
