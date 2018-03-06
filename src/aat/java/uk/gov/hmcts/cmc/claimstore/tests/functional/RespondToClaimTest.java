package uk.gov.hmcts.cmc.claimstore.tests.functional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class RespondToClaimTest extends BaseTest {

    @Autowired
    private FunctionalTestsUsers functionalTestsUsers;

    @Test
    public void shouldBeAbleToSuccessfullySubmitDisputeDefence() {
        Response fullDefenceDisputeResponse = SampleResponse.FullDefence.builder()
            .withDefenceType(FullDefenceResponse.DefenceType.DISPUTE)
            .build();
        shouldBeAbleToSuccessfullySubmit(fullDefenceDisputeResponse);
    }

    @Test
    public void shouldBeAbleToSuccessfullySubmitAlreadyPaidDefence() {
        Response fullDefenceAlreadyPaidResponse = SampleResponse.FullDefence.builder()
            .withDefenceType(FullDefenceResponse.DefenceType.ALREADY_PAID)
            .withMediation(null)
            .build();
        shouldBeAbleToSuccessfullySubmit(fullDefenceAlreadyPaidResponse);
    }

    private void shouldBeAbleToSuccessfullySubmit(Response response) {
        Claim createdCase = commonOperations.submitClaim(
            functionalTestsUsers.getClaimant().getAuthorisation(),
            functionalTestsUsers.getClaimant().getUserDetails().getId()
        );

        User defendant = functionalTestsUsers.createDefendant();
        commonOperations.linkDefendant(defendant.getAuthorisation());

        Claim updatedCase = commonOperations.submitResponse(response, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getResponse().isPresent()).isTrue();
        assertThat(updatedCase.getResponse().get()).isEqualTo(response);
        assertThat(updatedCase.getRespondedAt()).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidResponseIsSubmitted() {
        Claim createdCase = commonOperations.submitClaim(
            functionalTestsUsers.getClaimant().getAuthorisation(),
            functionalTestsUsers.getClaimant().getUserDetails().getId()
        );

        User defendant = functionalTestsUsers.createDefendant();
        commonOperations.linkDefendant(defendant.getAuthorisation());

        Response invalidResponse = SampleResponse.FullDefence.builder()
            .withDefence(null)
            .build();

        commonOperations.submitResponse(invalidResponse, createdCase.getExternalId(), defendant)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }
}
