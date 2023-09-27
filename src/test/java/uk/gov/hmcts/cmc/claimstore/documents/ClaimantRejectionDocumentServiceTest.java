package uk.gov.hmcts.cmc.claimstore.documents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.AddressMapper;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.DocAssemblyService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.DocAssemblyTemplateBody;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantRejectionDocumentServiceTest {

    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String JURISDICTION_ID = "CMC";

    private String defendantOconN9xClaimantMediation;
    private ClaimantRejectionDefendantDocumentService claimantRejectionDefendantDocumentService;
    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private AddressMapper addressMapper;

    @Before
    public void setUp() {
        defendantOconN9xClaimantMediation = "XYZ";

        claimantRejectionDefendantDocumentService
            = new ClaimantRejectionDefendantDocumentService(
            defendantOconN9xClaimantMediation, CASE_TYPE_ID, JURISDICTION_ID, docAssemblyService, caseMapper, addressMapper);
    }

    @Test
    public void shouldGenerateClaimantRejectionDocumentForDefendant() {

        Claim claim = SampleClaim.getDefault();
        CCDCase ccdCase = CCDCase.builder().build();

        when(caseMapper.to(eq(claim))).thenReturn(ccdCase);

        claimantRejectionDefendantDocumentService.createClaimantRejectionDocument(claim, AUTHORISATION_TOKEN);

        verify(docAssemblyService).generateDocument(any(CCDCase.class), eq(AUTHORISATION_TOKEN),
            any(DocAssemblyTemplateBody.class), eq(defendantOconN9xClaimantMediation),
            eq(CASE_TYPE_ID), eq(JURISDICTION_ID));
    }
}
