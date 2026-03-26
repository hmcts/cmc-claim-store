package uk.gov.hmcts.cmc.claimstore.documents;

import org.assertj.core.api.AssertionsForClassTypes;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantRejectionDocumentServiceTest {

    protected static final String AUTHORISATION_TOKEN = "Bearer token";
    private static final String CASE_TYPE_ID = "MoneyClaimCase";
    private static final String JURISDICTION_ID = "CMC";
    private static final String partyName = "Dr. John Smith";
    private static final String refernceNumber = "000MC001";
    private static final String claimantName = "John Rambo";
    private static final LocalDate CURRENT_DATE = LocalDate.now();

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

    @Test
    public void shouldGenerateDocAssemblyTemplateDocumentBody() {
        Claim claim = SampleClaim.getDefault();
        DocAssemblyTemplateBody requestBody =
            claimantRejectionDefendantDocumentService.defendantDetailsTemplateMapper(claim);
        DocAssemblyTemplateBody expectedBody = DocAssemblyTemplateBody.builder()
            .partyName(partyName)
            .currentDate(CURRENT_DATE)
            .referenceNumber(refernceNumber)
            .partyAddress(null)
            .claimantName(claimantName)
            .build();
        AssertionsForClassTypes.assertThat(requestBody).isEqualTo(expectedBody);
    }
}
