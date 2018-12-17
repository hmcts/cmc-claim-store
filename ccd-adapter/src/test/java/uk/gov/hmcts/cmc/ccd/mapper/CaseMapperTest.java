package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.SampleData;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static uk.gov.hmcts.cmc.ccd.SampleData.getAmountBreakDown;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;


@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseMapperTest {

    @Autowired
    private CaseMapper ccdCaseMapper;

    @Test
    public void shouldMapLegalClaimToCCD() {
        //given
        Claim claim = SampleClaim.getDefaultForLegal();

        //when
        CCDCase ccdCase = ccdCaseMapper.to(claim);

        //then
        assertThat(claim).isEqualTo(ccdCase);
    }

    @Test
    public void shouldMapCitizenClaimToCCD() {
        //given
        Claim claim = SampleClaim.getDefault();

        //when
        CCDCase ccdCase = ccdCaseMapper.to(claim);

        //then
        assertThat(claim).isEqualTo(ccdCase);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingClaimDataFromClaim() {
        //given
        Claim claim = SampleClaim.builder().withClaimData(null).build();

        //when
        ccdCaseMapper.to(claim);
    }

    @Test
    public void shouldMapLegalClaimFromCCD() {
        //given
        CCDCase ccdCase = SampleData.getCCDLegalCase();

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        assertThat(claim).isEqualTo(ccdCase);
    }

    @Test
    public void shouldMapCitizenClaimFromCCD() {
        //given
        CCDCase ccdCase = SampleData.getCCDCitizenCase(getAmountBreakDown());

        //when
        Claim claim = ccdCaseMapper.from(ccdCase);

        //then
        assertThat(claim).isEqualTo(ccdCase);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingClaimDataFromCCDCase() {
        //given
        CCDCase ccdCase = SampleData.getCCDCitizenCase(null);

        //when
        ccdCaseMapper.from(ccdCase);
    }
}
