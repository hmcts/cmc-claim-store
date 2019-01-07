package uk.gov.hmcts.cmc.ccd.assertion.mapper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantResponseMapperTest {

    @Autowired
    private ClaimantResponseMapper mapper;

    private Claim.ClaimBuilder claimBuilder;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        claimBuilder = Claim.builder();
    }

    @Test
    public void shouldMapClaimantAcceptanceWithCCJFormalisationToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder().build();
        Claim claim =  claimBuilder.claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation)ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantAcceptanceWithClaimantPaymentIntentionBySetDateToCCDClaimantAcceptance() {
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
    public void shouldMapClaimantAcceptanceWithCourtDeterminationPayByInstalmentsToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptanceIssueSettlementWithCourtDeterminationPayByInstalments();
        Claim claim =  claimBuilder.claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation)ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantAcceptanceWithClaimantPaymentIntentionPayImmediatelyToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptanceIssueSettlementWithClaimantPaymentIntentionPayImmediately();
        Claim claim =  claimBuilder.claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation)ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapCCDResponseAcceptationWithCCJFormalisationToResponseAcceptation() {
        CCDResponseAcceptation ccdResponse = SampleData.getResponseAcceptation(CCDFormaliseOption.CCJ);
        mapper.from(ccdResponse,claimBuilder);
        Claim claim = claimBuilder.build();
        if (claim.getClaimantResponse().isPresent()) {
            assertThat((ResponseAcceptation) claim.getClaimantResponse().get()).isEqualTo(ccdResponse);
        }
        assertNotNull(claim.getClaimantRespondedAt().orElse(null));
    }

    @Test
    public void shouldMapCCDResponseAcceptationWithClaimantPaymentIntentionImmediatelyToResponseAcceptation() {
        CCDResponseAcceptation ccdResponse = SampleData
            .getResponseAcceptationWithClaimantPaymentIntentionImmediately();
        mapper.from(ccdResponse,claimBuilder);
        Claim claim = claimBuilder.build();
        if (claim.getClaimantResponse().isPresent()) {
            assertThat((ResponseAcceptation) claim.getClaimantResponse().get()).isEqualTo(ccdResponse);
        }
        assertNotNull(claim.getClaimantRespondedAt().orElse(null));
    }

    @Test
    public void shouldMapCCDResponseAcceptationWithClaimantPaymentIntentionPayBySetDateToResponseAcceptation() {
        CCDResponseAcceptation ccdResponse = SampleData
            .getResponseAcceptationWithClaimantPaymentIntentionPayBySetDate();
        mapper.from(ccdResponse,claimBuilder);
        Claim claim = claimBuilder.build();
        if (claim.getClaimantResponse().isPresent()) {
            assertThat((ResponseAcceptation) claim.getClaimantResponse().get()).isEqualTo(ccdResponse);
        }
        assertNotNull(claim.getClaimantRespondedAt().orElse(null));
    }

    @Test
    public void shouldThrowExceptionWhenAttemptingToMapNullClaimantResponse() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("claim must not be null");
        mapper.to(null);
    }

    @Test
    public void shouldMapCCDResponseRejectionToResponseRejection() {
        CCDResponseRejection ccdResponse = SampleData.getResponseRejection();
        mapper.from(ccdResponse,claimBuilder);
        Claim claim = claimBuilder.build();
        if (claim.getClaimantResponse().isPresent()) {
            assertThat((ResponseRejection) claim.getClaimantResponse().get()).isEqualTo(ccdResponse);
        }
        assertNotNull(claim.getClaimantRespondedAt().orElse(null));
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
