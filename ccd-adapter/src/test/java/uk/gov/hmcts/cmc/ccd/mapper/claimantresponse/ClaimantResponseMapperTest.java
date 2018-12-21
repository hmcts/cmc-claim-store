package uk.gov.hmcts.cmc.ccd.mapper.claimantresponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDClaimantResponse;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseAcceptation;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDResponseRejection;
import uk.gov.hmcts.cmc.ccd.util.SampleData;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDateTime;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantResponseMapperTest {

    @Autowired
    private ClaimantResponseMapper claimantResponseMapper;

    @Test
    public void shouldMapClaimantAcceptanceWithCCJFormalisationFromCCD() {
        //given
        CCDResponseAcceptation ccdResponse = SampleData.getResponseAcceptation(FormaliseOption.CCJ);

        //when
        Claim.ClaimBuilder claimBuilder = Claim.builder();
        claimantResponseMapper.from(ccdResponse, claimBuilder);

        //then
        claimBuilder.build().getClaimantResponse().ifPresent(claimantResponse ->
            assertThat((ResponseAcceptation) claimantResponse).isEqualTo(ccdResponse));
    }


    @Test
    public void shouldMapCCDClaimantAcceptanceWithCCJFormalisationToClaimantAcceptance() {
        //given
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder().build();
        LocalDateTime claimantRespondedAt = LocalDateTimeFactory.nowInLocalZone();

        //when
        CCDClaimantResponse ccdResponse = claimantResponseMapper.to(response, claimantRespondedAt);

        //then
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
    }

    @Test
    public void shouldMapClaimantAcceptanceWithSettlementFormalisationFromCCD() {
        //given
        CCDResponseAcceptation ccdResponseAcceptation = SampleData.getResponseAcceptation(FormaliseOption.SETTLEMENT);

        //when
        Claim.ClaimBuilder claimBuilder = Claim.builder();
        claimantResponseMapper.from(ccdResponseAcceptation, claimBuilder);

        //then
        claimBuilder.build().getClaimantResponse().ifPresent(claimantResponse ->
            assertThat((ResponseAcceptation) claimantResponse).isEqualTo(ccdResponseAcceptation));
    }

    @Test
    public void shouldMapClaimantRejectionFromCCD() {
        //given
        CCDResponseRejection ccdResponse = SampleData.getResponseRejection();

        //when
        Claim.ClaimBuilder claimBuilder = Claim.builder();
        claimantResponseMapper.from(ccdResponse, claimBuilder);
        //then
        claimBuilder.build().getClaimantResponse().ifPresent(claimantResponse ->
            assertThat((ResponseRejection) claimantResponse).isEqualTo(ccdResponse));
    }

    @Test
    public void shouldMapCCDClaimantRejectionToClaimantRejection() {
        //given
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseRejection.builder().build();

        LocalDateTime claimantRespondedAt = LocalDateTimeFactory.nowInLocalZone();
        //when
        CCDClaimantResponse ccdResponse = claimantResponseMapper.to(response, claimantRespondedAt);

        //then
        assertThat((ResponseRejection) response).isEqualTo((CCDResponseRejection) ccdResponse);
    }
}
