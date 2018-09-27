package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseRejection;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.CCJ;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption.SETTLEMENT;

public class ClaimantResponseTest extends BaseTest {

    private User claimant;
    private Claim claim;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();

        String claimantId = claimant.getUserDetails().getId();
        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimantId
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        claim = createClaimWithResponse(createdCase, defendant);
    }

    @Test
    public void shouldSaveClaimantResponseAcceptationReferToJudge() {
        commonOperations.submitClaimantResponse(
            SampleClaimantResponse.validDefaultAcceptation(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt()).isNotEmpty();
        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getAmountPaid().orElse(ZERO)).isEqualTo(BigDecimal.TEN);
    }

    @Test
    public void shouldSaveClaimantResponseAcceptationIssueCCJWithDefendantPaymentIntention() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithDefendantPaymentIntention(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsCCJ(claimWithClaimantResponse);
    }

    private void assertClaimantResponseFormaliseAsCCJ(Claim claimWithClaimantResponse) {
        assertThat(claimWithClaimantResponse.getClaimantRespondedAt()).isNotEmpty();
        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getAmountPaid().orElse(ZERO)).isEqualTo(BigDecimal.TEN);
        assertThat(claimantResponse.getFormaliseOption()).isEqualTo(CCJ);
        CountyCourtJudgment countyCourtJudgment = claimWithClaimantResponse.getCountyCourtJudgment();
        assertThat(countyCourtJudgment).isNotNull();
        assertThat(countyCourtJudgment.getPayBySetDate()).isNotEmpty();
        assertThat(claimWithClaimantResponse.getCountyCourtJudgmentIssuedAt()).isNotEmpty();
    }

    @Test
    public void shouldSaveClaimantResponseAcceptationIssueCCJWithClaimantPaymentIntention() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithClaimantPaymentIntention(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsCCJ(claimWithClaimantResponse);
    }

    @Test
    public void shouldSaveClaimantResponseAcceptationIssueCCJWithCourtDetermination() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithCourtDetermination(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsCCJ(claimWithClaimantResponse);
    }

    @Test
    public void shouldSaveClaimantResponseAcceptationIssueSettlementWithCourtDetermination() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueSettlementWithCourtDetermination(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsSettlement(claimWithClaimantResponse);
    }

    @Test
    public void shouldSaveClaimantResponseAcceptationIssueSettlementWithDefendantPaymentIntention() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueSettlementWithDefendantPaymentIntention(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsSettlement(claimWithClaimantResponse);
    }

    @Test
    public void fshouldSaveClaimantResponseAcceptationIssueSettlementWithClaimantPaymentIntention() {
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueSettlementWithClaimantPaymentIntention(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertClaimantResponseFormaliseAsSettlement(claimWithClaimantResponse);
    }

    private void assertClaimantResponseFormaliseAsSettlement(Claim claimWithClaimantResponse) {
        assertThat(claimWithClaimantResponse.getClaimantRespondedAt()).isNotEmpty();
        ResponseAcceptation claimantResponse = (ResponseAcceptation) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getAmountPaid().orElse(ZERO)).isEqualTo(BigDecimal.TEN);
        assertThat(claimantResponse.getFormaliseOption()).isEqualTo(SETTLEMENT);
        assertThat(claimWithClaimantResponse.getCountyCourtJudgment()).isNull();
        assertThat(claimWithClaimantResponse.getSettlement()).isNotEmpty();
    }

    @Test
    public void shouldSaveClaimantResponseRejection() {
        commonOperations.submitClaimantResponse(
            SampleClaimantResponse.validDefaultRejection(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        Claim claimWithClaimantResponse = commonOperations
            .retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithClaimantResponse.getClaimantRespondedAt()).isNotEmpty();

        ResponseRejection claimantResponse = (ResponseRejection) claimWithClaimantResponse.getClaimantResponse()
            .orElseThrow(AssertionError::new);

        assertThat(claimantResponse.getFreeMediation()).isNotEmpty();
        assertThat(claimantResponse.getAmountPaid().orElse(ZERO)).isEqualTo(BigDecimal.TEN);
    }

    private Claim createClaimWithResponse(Claim createdCase, User defendant) {
        commonOperations.linkDefendant(
            defendant.getAuthorisation()
        );

        Response response = SampleResponse.PartAdmission.builder()
            .buildWithPaymentOptionBySpecifiedDate();

        return commonOperations.submitResponse(response, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);
    }
}
