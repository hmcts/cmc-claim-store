package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement.SettlementAgreementPDFContentProvider;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SettlementAgreementCopyServiceTest {
    @Mock
    private SettlementAgreementPDFContentProvider contentProvider;
    @Mock
    private DocumentTemplates documentTemplates;
    @Mock
    private PDFServiceClient pdfServiceClient;

    private SettlementAgreementCopyService settlementAgreementCopyService;

    @Before
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
            Assert.fail("Expected a NotFoundException to be thrown");
        } catch (NotFoundException expected) {
            assertThat(expected).hasMessage("Settlement Agreement does not exist for this claim");
        }
    }
}
