package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.document.LegacyDocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.document.SecuredDocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

    private final boolean secureDocumentManagement = false;

    private DocumentManagementService documentManagementService;

    private DocumentManagementService secureDocumentManagementService;

    @BeforeEach
    void setUp() {
        documentManagementService = mock(LegacyDocumentManagementService.class);
        secureDocumentManagementService = mock(SecuredDocumentManagementService.class);
        printableDocumentService = new PrintableDocumentService(secureDocumentManagement, documentManagementService, secureDocumentManagementService);
    }

    @Test
    void shouldDownloadDocumentFromDocumentManagement() {

        when(documentManagementService.downloadDocument(anyString(), any(ClaimDocument.class)))
            .thenReturn(new byte[]{1, 2, 3, 4});

        printableDocumentService.pdf(document, AUTHORISATION);

        verify(documentManagementService, times(1)).downloadDocument(
            anyString(),
            any(ClaimDocument.class));
    }

    @Test
    void shouldThrowExceptionIfDownloadUrlIsWrong() {

        when(documentManagementService.downloadDocument(
            anyString(),
            any(ClaimDocument.class))).thenThrow(new IllegalArgumentException("Exception"));

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> printableDocumentService.pdf(document, AUTHORISATION));

    }

    @Test
    void shouldThrowExceptionIfUrlIsWrong() {

        when(documentManagementService.downloadDocument(
            anyString(),
            any(ClaimDocument.class))).thenThrow(new IllegalArgumentException("Exception"));

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> printableDocumentService.process(document, AUTHORISATION));

    }
}
