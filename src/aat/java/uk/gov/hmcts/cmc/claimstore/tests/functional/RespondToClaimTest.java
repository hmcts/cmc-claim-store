package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
            bootstrap.getCitizenUser().getAuthorisation(),
            bootstrap.getCitizenUser().getUserDetails().getId()
        );

        User defendant = idamTestService.createDefendant();

        commonOperations.linkDefendant(
            createdCase.getExternalId(),
            defendant.getAuthorisation(),
            defendant.getUserDetails().getId()
        );

        Claim updatedCase = respondToClaim(createdCase.getExternalId(), defendant, response)
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
            bootstrap.getCitizenUser().getAuthorisation(),
            bootstrap.getCitizenUser().getUserDetails().getId()
        );

        User defendant = idamTestService.createDefendant();

        commonOperations.linkDefendant(
            createdCase.getExternalId(),
            defendant.getAuthorisation(),
            defendant.getUserDetails().getId()
        );

        Response invalidResponse = SampleResponse.FullDefence.builder()
            .withDefence(null)
            .build();

        respondToClaim(createdCase.getExternalId(), defendant, invalidResponse)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    private io.restassured.response.Response respondToClaim(String claimExternalId, User defendant, Response response) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .body(jsonMapper.toJson(response))
            .when()
            .post("/responses/claim/" + claimExternalId + "/defendant/" + defendant.getUserDetails().getId());
    }

}
