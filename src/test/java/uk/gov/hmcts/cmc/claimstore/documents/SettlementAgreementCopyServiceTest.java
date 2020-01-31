package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.config.properties.pdf.DocumentTemplates;
import uk.gov.hmcts.cmc.claimstore.documents.content.settlementagreement.SettlementAgreementPDFContentProvider;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotFoundException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;

@RunWith(MockitoJUnitRunner.class)
public class SettlementAgreementCopyServiceTest {
    @Rule
    public final ExpectedException exceptionRule = ExpectedException.none();
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
        exceptionRule.expect(NotFoundException.class);
        exceptionRule.expectMessage("Settlement Agreement does not exist for this claim");
        settlementAgreementCopyService.createPdf(claim);
    }
}
