package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDMapperConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@SpringBootTest
@ContextConfiguration(classes = CCDMapperConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CCDCaseDataToClaimTest {

    private CCDCaseDataToClaim ccdCaseDataToClaim;

    @Autowired
    private CaseMapper caseMapper;

    @Before
    public void setup() {
        ccdCaseDataToClaim = new CCDCaseDataToClaim(caseMapper, JsonMapperFactory.create());
    }

    @Test
    public void convertsCaseDetailsToCCDCase() {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
        CCDCase ccdCase = ccdCaseDataToClaim.convertToCCDCase(caseDetails);
        assertThat(ccdCase.getId()).isEqualTo(caseDetails.getId());
        assertThat(ccdCase.getState()).isEqualTo(caseDetails.getState());
    }

    @Test
    public void convertsCaseDetailsToCCDClaim() {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
        Claim claim = ccdCaseDataToClaim.to(caseDetails);
        assertThat(claim.getId()).isEqualTo(caseDetails.getId());
        assertThat(claim.getState()).isPresent();
        assertThat(claim.getState().orElseThrow(AssertionError::new).getValue()).isEqualTo(caseDetails.getState());
    }
}
