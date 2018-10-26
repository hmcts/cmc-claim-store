package uk.gov.hmcts.cmc.claimstore.tests.functional.citizen;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimantResponse.ClaimantResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class ReDeterminationTest extends BaseTest {

    private User claimant;
    private Claim claim;

    @Before
    public void before() {
        claimant = idamTestService.createCitizen();

        Claim createdCase = commonOperations.submitClaim(
            claimant.getAuthorisation(),
            claimant.getUserDetails().getId()
        );

        User defendant = idamTestService.createDefendant(createdCase.getLetterHolderId());
        claim = createClaimWithResponse(createdCase, defendant);
    }

    @Test
    public void shouldSaveReDeterminationWithCourtDetermination() {
        String explanation = "I want it sooner";
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithCourtDetermination(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.submitReDetermination(
            ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.OK.value());

        Claim claimWithReDetermination
            = commonOperations.retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithReDetermination.getReDeterminationRequestedAt()).isNotEmpty();
        ReDetermination reDetermination = claimWithReDetermination.getReDetermination()
            .orElseThrow(AssertionError::new);

        assertThat(reDetermination.getExplanation()).isEqualTo(explanation);
    }

    @Test
    public void shouldSaveReDeterminationWithoutCourtDetermination() {
        String explanation = "I want it sooner";
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithClaimantPaymentIntention(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.submitReDetermination(
            ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.OK.value());

        Claim claimWithReDetermination
            = commonOperations.retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithReDetermination.getReDeterminationRequestedAt()).isNotEmpty();
        ReDetermination reDetermination = claimWithReDetermination.getReDetermination()
            .orElseThrow(AssertionError::new);

        assertThat(reDetermination.getExplanation()).isEqualTo(explanation);
    }

    @Test
    public void shouldSaveReDeterminationWithDefendantPaymentIntentionAccepted() {
        String explanation = "I want it sooner";
        commonOperations.submitClaimantResponse(
            ClaimantResponseAcceptation.builder().buildAcceptationIssueCCJWithDefendantPaymentIntention(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.CREATED.value());

        commonOperations.submitReDetermination(
            ReDetermination.builder().explanation(explanation).partyType(MadeBy.CLAIMANT).build(),
            claim.getExternalId(),
            claimant
        ).then()
            .statusCode(HttpStatus.OK.value());

        Claim claimWithReDetermination
            = commonOperations.retrieveClaim(claim.getExternalId(), claimant.getAuthorisation());

        assertThat(claimWithReDetermination.getReDeterminationRequestedAt()).isNotEmpty();
        ReDetermination reDetermination = claimWithReDetermination.getReDetermination()
            .orElseThrow(AssertionError::new);

        assertThat(reDetermination.getExplanation()).isEqualTo(explanation);
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
