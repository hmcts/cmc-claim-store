package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentsService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;

@RunWith(MockitoJUnitRunner.class)
public class DocumentsControllerTest {

    private static final String AUTHORISATION = "Bearer: let me in";

    @Mock
    private DocumentsService documentsService;

    private DocumentsController documentsController;

    private Claim claim;

    @Before
    public void setUp() {
        documentsController = new DocumentsController(
            documentsService
        );
        claim = SampleClaim.builder().build();
    }

    @Test(expected = RuntimeException.class)
    public void shouldGenerateDocumentForValidDocumentType() {
        documentsController.document(
            "sealedClaim",
            claim.getExternalId(),
            AUTHORISATION
        );
        verify(documentsService.generateDocument(
            claim.getExternalId(),
            SEALED_CLAIM,
            AUTHORISATION
        ));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowIfDocumentTypeIsUnknown() {
        documentsController.document(
            "bla",
            claim.getExternalId(),
            AUTHORISATION
        );
    }
}
