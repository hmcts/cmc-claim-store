package uk.gov.hmcts.cmc.ccd.mapper;

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
import uk.gov.hmcts.cmc.ccd.mapper.claimantresponse.ClaimantResponseMapper;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.NO;
import static uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption.YES;
import static uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDFormaliseOption.CCJ;

@SpringBootTest
@ContextConfiguration(classes = CCDAdapterConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ClaimantResponseMapperTest {

    @Autowired
    private ClaimantResponseMapper mapper;

    @Test
    public void shouldMapClaimantAcceptanceWithCCJFormalisationToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder().build();
        Claim claim = Claim.builder().claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantAcceptanceWithClaimantPaymentIntentionBySetDateToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptationIssueCCJWithClaimantPaymentIntentionBySetDate();
        Claim claim = Claim.builder().claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantAcceptanceWithCourtDeterminationPayByInstalmentsToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptanceIssueSettlementWithCourtDeterminationPayByInstalments();

        Claim claim = Claim.builder().claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();

        CCDClaimantResponse ccdResponse = mapper.to(claim);

        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantAcceptanceWithClaimantPaymentIntentionPayImmediatelyToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptanceIssueSettlementWithClaimantPaymentIntentionPayImmediately();
        Claim claim = Claim.builder().claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantAcceptanceSettlePreJudgementToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseAcceptation.builder()
            .buildAcceptanceSettlePreJudgement();
        Claim claim = Claim.builder().claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantRejectionSettlePreJudgementToCCDClaimantAcceptance() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseRejection.builder()
            .buildRejectionSettlePreJudgement();
        Claim claim = Claim.builder().claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseAcceptation) response).isEqualTo((CCDResponseAcceptation) ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapClaimantRejectionDQtoCCDDirectionsQuestionnaire() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseRejection.builder()
            .buildRejectionWithDirectionsQuestionnaire();
        Claim claim = Claim.builder().claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();
        CCDClaimantResponse ccdResponse = mapper.to(claim);
        assertThat((ResponseRejection) response).isEqualTo((CCDResponseRejection) ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapCCDResponseAcceptationWithCCJFormalisationToResponseAcceptation() {
        CCDResponseAcceptation ccdResponse = SampleData.getResponseAcceptation(CCJ);
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        mapper.from(ccdResponse, claimBuilder);
        Claim claim = claimBuilder.build();

        assertThat(claim.getClaimantResponse()).isPresent();
        ResponseAcceptation claimantResponse = (ResponseAcceptation) claim.getClaimantResponse().get();
        assertThat(claimantResponse).isEqualTo(ccdResponse);
        assertThat(claim.getClaimantRespondedAt()).isPresent();
    }

    @Test
    public void shouldMapCCDResponseAcceptationWithClaimantPaymentIntentionImmediatelyToResponseAcceptation() {
        CCDResponseAcceptation ccdResponse = SampleData
            .getResponseAcceptationWithClaimantPaymentIntentionImmediately();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        mapper.from(ccdResponse, claimBuilder);
        Claim claim = claimBuilder.build();

        assertThat(claim.getClaimantResponse()).isPresent();
        assertThat((ResponseAcceptation) claim.getClaimantResponse()
            .orElseThrow(() -> new AssertionError("Missing claimant response")))
            .isEqualTo(ccdResponse);
        assertThat(claim.getClaimantRespondedAt()).isPresent();
    }

    @Test
    public void shouldMapCCDResponseAcceptationWithClaimantPaymentIntentionPayBySetDateToResponseAcceptation() {
        CCDResponseAcceptation ccdResponse = SampleData
            .getResponseAcceptationWithClaimantPaymentIntentionPayBySetDate();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        mapper.from(ccdResponse, claimBuilder);
        Claim claim = claimBuilder.build();

        assertThat(claim.getClaimantResponse()).isPresent();
        assertThat((ResponseAcceptation) claim.getClaimantResponse()
            .orElseThrow(() -> new AssertionError("Missing claimant response")))
            .isEqualTo(ccdResponse);
        assertThat(claim.getClaimantRespondedAt()).isPresent();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenAttemptingToMapNullClaimantResponse() {
        mapper.to(null);
    }

    @Test
    public void shouldMapCCDResponseRejectionToResponseRejection() {
        CCDResponseRejection ccdResponse = SampleData.getResponseRejection();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        mapper.from(ccdResponse, claimBuilder);
        Claim claim = claimBuilder.build();

        assertThat(claim.getClaimantResponse()).isPresent();
        assertThat((ResponseRejection) claim.getClaimantResponse()
            .orElseThrow(() -> new AssertionError("Missing claimant response")))
            .isEqualTo(ccdResponse);
        assertThat(claim.getClaimantRespondedAt()).isPresent();
    }

    @Test
    public void shouldMapCCDClaimantRejectionToClaimantRejection() {
        ClaimantResponse response = SampleClaimantResponse.ClaimantResponseRejection.builder().build();
        Claim claim = Claim.builder()
            .claimantResponse(response)
            .claimantRespondedAt(LocalDateTimeFactory.nowInLocalZone())
            .build();

        CCDClaimantResponse ccdResponse = mapper.to(claim);

        assertThat((ResponseRejection) response).isEqualTo((CCDResponseRejection) ccdResponse);
        assertNotNull(ccdResponse.getSubmittedOn());
    }

    @Test
    public void shouldMapCCDResponseAcceptationForSettlePreJudgementToResponseAcceptation() {
        CCDResponseAcceptation ccdResponse = CCDResponseAcceptation.builder()
            .settleForAmount(YES)
            .paymentReceived(YES)
            .submittedOn(now())
            .build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        mapper.from(ccdResponse, claimBuilder);
        Claim claim = claimBuilder.build();

        assertThat(claim.getClaimantResponse()).isPresent();
        assertThat((ResponseAcceptation) claim.getClaimantResponse()
            .orElseThrow(() -> new AssertionError("Missing claimant response")))
            .isEqualTo(ccdResponse);
        assertThat(claim.getClaimantRespondedAt()).isPresent();
    }

    @Test
    public void shouldMapCCDResponseRejectionForRejectSettlePreJudgementToResponseRejection() {
        CCDResponseRejection ccdResponse = CCDResponseRejection.builder()
            .settleForAmount(NO)
            .paymentReceived(YES)
            .submittedOn(now())
            .build();
        Claim.ClaimBuilder claimBuilder = Claim.builder();

        mapper.from(ccdResponse, claimBuilder);
        Claim claim = claimBuilder.build();

        assertThat(claim.getClaimantResponse()).isPresent();
        assertThat((ResponseRejection) claim.getClaimantResponse()
            .orElseThrow(() -> new AssertionError("Missing claimant response")))
            .isEqualTo(ccdResponse);
        assertThat(claim.getClaimantRespondedAt()).isPresent();
    }

}

