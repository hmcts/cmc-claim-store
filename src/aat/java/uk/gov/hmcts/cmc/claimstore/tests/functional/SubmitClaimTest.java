package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class SubmitClaimTest extends BaseTest {

    @Autowired
    private FunctionalTestsUsers functionalTestsUsers;

    @Test
    public void shouldSuccessfullySubmitClaimDataAndReturnCreatedCase() {
        ClaimData claimData = testData.submittedByClaimantBuilder().build();

        Claim createdCase = submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(createdCase.getClaimData()).isEqualTo(claimData);
        assertThat(createdCase.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));
    }

    @Test
    public void shouldReturnUnprocessableEntityWhenInvalidClaimIsSubmitted() {
        ClaimData invalidClaimData = testData.submittedByClaimantBuilder()
            .withAmount(null)
            .build();

        submitClaim(invalidClaimData)
            .then()
            .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    public void shouldReturnConflictResponseWhenClaimDataWithDuplicatedExternalIdIsSubmitted() {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = testData.submittedByClaimantBuilder()
            .withExternalId(externalId)
            .build();

        submitClaim(claimData)
            .andReturn();
        submitClaim(claimData)
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    private Response submitClaim(ClaimData claimData) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, functionalTestsUsers.getClaimant().getAuthorisation())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + functionalTestsUsers.getClaimant().getUserDetails().getId());
    }

}
