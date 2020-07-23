package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DraftClaimReceiptServiceTest {
    private static final byte[] PDF_BYTES = {1, 2, 3};

    @Mock
    private PDFServiceClient pdfServiceClient;

    @Mock
    private CitizenServiceDocumentsService citizenServiceDocumentsService;

    private DraftClaimReceiptService service;

    @BeforeEach
    void setUp() {
        service = new DraftClaimReceiptService(pdfServiceClient, citizenServiceDocumentsService);
    }

    @Test
    void shouldThrowExceptionForNullClaim() {
        assertThatThrownBy(() -> service.createPdf(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldCallServices() {
        when(citizenServiceDocumentsService.draftClaimDocument(any(Claim.class)))
            .thenReturn(new Document("template", Collections.emptyMap()));
        when(pdfServiceClient.generateFromHtml(any(byte[].class), anyMap()))
            .thenReturn(PDF_BYTES);

        PDF result = service.createPdf(Claim.builder().externalId("1").build());
        assertThat(result).isNotNull();

        verify(citizenServiceDocumentsService).draftClaimDocument(any(Claim.class));
        verify(pdfServiceClient).generateFromHtml(any(byte[].class), anyMap());
    }
}
