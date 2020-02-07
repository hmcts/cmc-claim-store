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
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@ExtendWith(MockitoExtension.class)
class DocumentsControllerTest {

    private static final String AUTHORISATION = "Bearer: let me in";

    @Mock
    private DocumentsService documentsService;

    private DocumentsController documentsController;

    private Claim claim;

    @BeforeEach
    void setUp() {
        documentsController = new DocumentsController(
            documentsService
        );
        claim = SampleClaim.builder().build();
    }

    @Test
    void shouldGenerateDocumentForValidDocumentType() {
        assertThrows(RuntimeException.class,
            () -> documentsController.document("sealedClaim", claim.getExternalId(), AUTHORISATION));
        verify(documentsService).generateDocument(claim.getExternalId(), SEALED_CLAIM, AUTHORISATION);
    }

    @Test
    void shouldThrowIfDocumentTypeIsUnknown() {
        assertThrows(RuntimeException.class,
            () -> documentsController.document("bla", claim.getExternalId(), AUTHORISATION));
    }
}
