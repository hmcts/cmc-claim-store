package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentSubtype.OCON9X;
import static uk.gov.hmcts.cmc.domain.models.ScannedDocumentType.FORM;

@ExtendWith(MockitoExtension.class)
class ScannedDocumentsControllerTest {

    private static final String AUTHORISATION = "Bearer: let me in";

    @Mock
    private DocumentsService documentsService;

    private ScannedDocumentsController scannedDocumentsController;

    private Claim claim;

    @BeforeEach
    void setUp() {
        scannedDocumentsController = new ScannedDocumentsController(
            documentsService
        );
        claim = SampleClaim.builder().build();
    }

    @Test
    void shouldRetrieveOCON9XForm() {

        byte[] pdfDocument = new byte[]{1, 2, 3};

        when(documentsService.generateScannedDocument(claim.getExternalId(), FORM, OCON9X, AUTHORISATION))
            .thenReturn(pdfDocument);

        ResponseEntity<ByteArrayResource> response = scannedDocumentsController.scannedDocument(claim.getExternalId(),
            "FORM", "OCON9X", AUTHORISATION);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pdfDocument, response.getBody().getByteArray());
    }
}
