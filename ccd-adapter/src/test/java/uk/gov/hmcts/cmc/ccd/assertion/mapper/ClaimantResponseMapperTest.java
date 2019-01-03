package uk.gov.hmcts.cmc.ccd.assertion.mapper;

import org.junit.Assert;
import org.junit.Before;
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

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption.SETTLEMENT;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantResponseMapperTest {

    @Autowired
    private ClaimantResponseMapper mapper;

    private Claim.ClaimBuilder claimBuilder;

    @Before
    public void setUp() {
        claimBuilder = Claim.builder();
    }

    @Test
    public void shouldMapCCDClaimantAcceptanceWithCCJFormalisationToClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder().build();
        Claim claim =  claimBuilder.claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation)ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapCCDClaimantAcceptanceWithIssueCCJWithClaimantPaymentIntentionBySetDateToClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptationIssueCCJWithClaimantPaymentIntentionBySetDate();
        Claim claim =  claimBuilder.claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation)ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantAcceptanceWithCCJFormalisationFromCCD() {
        CCDResponseAcceptation ccdResponse = SampleData.getResponseAcceptation(CCDFormaliseOption.CCJ);
        mapper.from(ccdResponse,claimBuilder);
        Claim claim = claimBuilder.build();
        if(claim.getClaimantResponse().isPresent()) {
            assertThat((ResponseAcceptation) claim.getClaimantResponse().get()).isEqualTo(ccdResponse);
        }
        claim.getClaimantRespondedAt().ifPresent(Assert::assertNotNull);
    }


    @Test
    public void shouldMapClaimantAcceptanceWithSettlementFormalisationFromCCD() {
        CCDResponseAcceptation ccdResponse = SampleData.getResponseAcceptation(SETTLEMENT);
        mapper.from(ccdResponse,claimBuilder);
        Claim claim = claimBuilder.build();
        if(claim.getClaimantResponse().isPresent()) {
            assertThat((ResponseAcceptation) claim.getClaimantResponse().get()).isEqualTo(ccdResponse);
        }
        claim.getClaimantRespondedAt().ifPresent(Assert::assertNotNull);
    }


    /********************** Rejection *********************************/

    @Test
    public void shouldMapClaimantRejectionFromCCD() {
        CCDResponseRejection ccdResponse = SampleData.getResponseRejection();
        mapper.from(ccdResponse,claimBuilder);
        Claim claim = claimBuilder.build();
        if(claim.getClaimantResponse().isPresent()) {
            assertThat((ResponseRejection) claim.getClaimantResponse().get()).isEqualTo(ccdResponse);
        }
        claim.getClaimantRespondedAt().ifPresent(Assert::assertNotNull);
    }

    @Test
    public void shouldMapCCDClaimantRejectionToClaimantRejection() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseRejection.builder().build();
        Claim claim =  Claim.builder()
            .claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseRejection)response).isEqualTo((CCDResponseRejection)ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

}
