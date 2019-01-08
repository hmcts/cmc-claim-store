package uk.gov.hmcts.cmc.ccd.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.mapper.claimantresponse.ClaimantResponseMapper;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption.SETTLEMENT;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantResponseMapperTest {

    @Autowired
    private ClaimantResponseMapper mapper;

    @Test
    public void shouldMapClaimantAcceptanceWithCCJFormalisationFromCCD() {
        //given
        CCDResponseAcceptation ccdResponse = SampleData.getResponseAcceptation(CCDFormaliseOption.CCJ);

        //when
        ResponseAcceptation response = (ResponseAcceptation) mapper.from(ccdResponse).getClaimantResponse().get();

        //then
        assertThat(response).isEqualTo(ccdResponse);
    }

    @Test
    public void shouldMapCCDClaimantAcceptanceWithCCJFormalisationToClaimantAcceptance() {
        //given
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder().build();
        Claim claim = Claim.builder()
            .claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        //when
        CCDClaimantResponse ccdResponse = mapper.to(claim);

        //then
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
    }

    @Test
    public void shouldMapClaimantAcceptanceWithSettlementFormalisationFromCCD() {
        //given
        CCDClaimantResponse ccdResponse = SampleData.getResponseAcceptation(SETTLEMENT);

        //when
        ClaimantResponse response = mapper.from(ccdResponse).getClaimantResponse().get();

        //then
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
    }

    @Test
    public void shouldMapClaimantRejectionFromCCD() {
        //given
        CCDResponseRejection ccdResponse = SampleData.getResponseRejection();

        //when
        ClaimantResponse response = mapper.from(ccdResponse).getClaimantResponse().get();

        //then
        assertThat((ResponseRejection) response).isEqualTo(ccdResponse);
    }

    @Test
    public void shouldMapCCDClaimantRejectionToClaimantRejection() {
        //given
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseRejection.builder().build();
        Claim claim = Claim.builder()
            .claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        //when
        CCDClaimantResponse ccdResponse = mapper.to(claim);

        //then
        assertThat((ResponseRejection) response).isEqualTo((CCDResponseRejection) ccdResponse);
    }

    @Test
    public void shouldMapResponseRejectionFromCCD() {
        //given
        CCDResponseRejection ccdResponseRejection = SampleData.getResponseRejection();

        //when
        ResponseRejection response = (ResponseRejection) mapper.from(ccdResponseRejection).getClaimantResponse().get();

        //then
        assertThat(response).isEqualTo(ccdResponseRejection);
    }

    @Test
    public void shouldMapResponseAcceptanceWithCCJFormalisationFromCCD() {
        //given
        CCDResponseAcceptation ccdResponseAcceptation = SampleData.getResponseAcceptation(CCDFormaliseOption.CCJ);

        //when
        ResponseAcceptation response = (ResponseAcceptation) mapper.from(ccdResponseAcceptation)
            .getClaimantResponse().orElse(null);

        //then
        assertThat(response).isEqualTo(ccdResponseAcceptation);
    }

    @Test
    public void shouldMapResponseAcceptanceWithSettlementFormalisationFromCCD() {
        //given
        CCDResponseAcceptation ccdResponseAcceptation = SampleData.getResponseAcceptation(SETTLEMENT);

        //when
        ResponseAcceptation response = (ResponseAcceptation) mapper.from(ccdResponseAcceptation)
            .getClaimantResponse().orElse(null);

        //then
        assertThat(response).isEqualTo(ccdResponseAcceptation);
    }
}
