package uk.gov.hmcts.cmc.ccd.mapper;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getAmountBreakDown;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseMapperTest {

    @Autowired
    private CaseMapper ccdCaseMapper;

    @Test
    public void shouldMapLegalClaimToCCD() {
        //given
        Claim claim = SampleClaim.getLegalDataWithReps();

        //when
        CCDCase ccdCase = ccdCaseMapper.to(claim);

        //then
        assertThat(claim).isEqualTo(ccdCase);
        Assertions.assertThat(ccdCase.getMigrated()).isEqualTo(NO);
    }

    @Test
    public void shouldMapCitizenClaimToCCD() {
        //given
        Claim claim = SampleClaim.withFullClaimData();

        //when
        CCDCase ccdCase = ccdCaseMapper.to(claim);

        //then
        assertThat(claim).isEqualTo(ccdCase);
        Assertions.assertThat(ccdCase.getMigrated()).isEqualTo(NO);
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
