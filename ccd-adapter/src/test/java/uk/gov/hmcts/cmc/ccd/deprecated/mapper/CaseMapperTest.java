package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.util.UUID;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDCitizenClaim;
import static uk.gov.hmcts.cmc.ccd.util.SampleData.getCCDLegalClaim;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseMapperTest {

    @Autowired
    private CaseMapper caseMapper;

    @Test
    public void shouldMapLegalClaimToCCD() {
        //given
        Claim claim = SampleClaim.getDefaultForLegal();

        //when
        CCDCase ccdCase = caseMapper.to(claim);

        //then
        assertThat(claim).isEqualTo(ccdCase);
    }

    @Test
    public void shouldMapCitizenClaimToCCD() {
        //given
        Claim claim = SampleClaim.getDefault();

        //when
        CCDCase ccdCase = caseMapper.to(claim);

        //then
        assertThat(claim).isEqualTo(ccdCase);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingClaimDataFromClaim() {
        //given
        Claim claim = SampleClaim.builder().withClaimData(null).build();

        //when
        caseMapper.to(claim);
    }

    @Test
    public void shouldMapLegalClaimFromCCD() {
        //given
        CCDCase ccdCase = CCDCase.builder()
            .id(1L)
            .submittedOn("2017-11-01T10:15:30")
            .issuedOn("2017-11-15")
            .submitterEmail("my@email.com")
            .submitterId("123")
            .referenceNumber("ref no")
            .externalId(UUID.randomUUID().toString())
            .claimData(getCCDLegalClaim())
            .features("admissions")
            .build();

        //when
        Claim claim = caseMapper.from(ccdCase);

        //then
        assertThat(claim).isEqualTo(ccdCase);
    }

    @Test
    public void shouldMapCitizenClaimFromCCD() {
        //given
        CCDCase ccdCase = CCDCase.builder()
            .id(1L)
            .submittedOn("2017-11-01T10:15:30")
            .issuedOn("2017-11-15")
            .submitterEmail("my@email.com")
            .submitterId("123")
            .referenceNumber("ref no")
            .externalId(UUID.randomUUID().toString())
            .claimData(getCCDCitizenClaim())
            .features("admissions")
            .build();

        //when
        Claim claim = caseMapper.from(ccdCase);

        //then
        assertThat(claim).isEqualTo(ccdCase);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenMissingClaimDataFromCCDCase() {
        //given
        CCDCase ccdCase = CCDCase.builder()
            .id(1L)
            .submittedOn("2017-11-01T10:15:30")
            .issuedOn("2017-11-15")
            .submitterEmail("my@email.com")
            .submitterId("123")
            .referenceNumber("ref no")
            .externalId(UUID.randomUUID().toString())
            .claimData(null)
            .build();

        //when
        caseMapper.from(ccdCase);
    }
}
