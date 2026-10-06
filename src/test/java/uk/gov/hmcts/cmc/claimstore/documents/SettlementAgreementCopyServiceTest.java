package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement.SettlementAgreementPDFContentProvider;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SettlementAgreementCopyServiceTest {
    @Mock
    private SettlementAgreementPDFContentProvider contentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    private SettlementAgreementCopyService settlementAgreementCopyService;

    @BeforeEach
    public void setUp() {
        settlementAgreementCopyService = new SettlementAgreementCopyService(
            contentProvider,
            documentTemplates,
            pdfServiceClient
        );
    }

    @Test
    public void shouldThrowErrorWhenSettlementDoesNotExist() {
        Claim claim = SampleClaim.getDefault();
        try {
            settlementAgreementCopyService.createPdf(claim);
            Assertions.fail("Expected a NotFoundException to be thrown");
        } catch (NotFoundException expected) {
            assertThat(expected).hasMessage("Settlement Agreement does not exist for this claim");
        }
    }
}
