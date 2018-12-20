package uk.gov.hmcts.cmc.ccd.deprecated.mapper.claimantresponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.claimantresponse.CCDFormaliseOption.SETTLEMENT;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantResponseMapperTest {

    @Autowired
    private ClaimantResponseMapper mapper;

    @Test
    public void shouldMapClaimantAcceptanceWithCCJFormalisationFromCCD() {
        //given
        CCDClaimantResponse ccdResponse = SampleData.getCCDClaimantAcceptanceResponse(CCDFormaliseOption.CCJ);

        //when
        ClaimantResponse response = mapper.from(ccdResponse);

        //then
        assertThat(response).isEqualTo(ccdResponse);
    }


    @Test
    public void shouldMapCCDClaimantAcceptanceWithCCJFormalisationToClaimantAcceptance() {
        //given
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder().build();

        //when
        CCDClaimantResponse ccdResponse = mapper.to(response);

        //then
        assertThat(response).isEqualTo(ccdResponse);
    }

    @Test
    public void shouldMapClaimantAcceptanceWithSettlementFormalisationFromCCD() {
        //given
        CCDClaimantResponse ccdResponse = SampleData.getCCDClaimantAcceptanceResponse(SETTLEMENT);

        //when
        ClaimantResponse response = mapper.from(ccdResponse);

        //then
        assertThat(response).isEqualTo(ccdResponse);
    }

    @Test
    public void shouldMapClaimantRejectionFromCCD() {
        //given
        CCDClaimantResponse ccdResponse = SampleData.getCCDClaimantRejectionResponse();

        //when
        ClaimantResponse response = mapper.from(ccdResponse);

        //then
        assertThat(response).isEqualTo(ccdResponse);
    }

    @Test
    public void shouldMapCCDClaimantRejectionToClaimantRejection() {
        //given
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseRejection.builder().build();

        //when
        CCDClaimantResponse ccdResponse = mapper.to(response);

        //then
        assertThat(response).isEqualTo(ccdResponse);
    }
}
