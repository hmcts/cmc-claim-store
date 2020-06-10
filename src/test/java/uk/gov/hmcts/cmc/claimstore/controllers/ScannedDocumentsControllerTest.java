package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

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
        assertThrows(RuntimeException.class,
            () -> scannedDocumentsController.ocon9xForm(claim.getExternalId(), AUTHORISATION));
        verify(documentsService).getOCON9xForm(claim.getExternalId(), AUTHORISATION);
    }
}
