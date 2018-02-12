package uk.gov.hmcts.cmc.claimstore.tests.functional;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class RespondToClaimTest extends BaseTest {

    @Test
    public void shouldBeAbleToSuccessfullyRespondToClaim() {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder()
            .withExternalId(externalId)
            .build();

        saveClaim(claimData)
            .andReturn();

        User defendant = idamTestService.createDefendant();

        // TODO Link
        Response response = SampleResponse.validDefaults();

        Claim updatedCase = respondToClaim(externalId.toString(), defendant, response)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().as(Claim.class);

        assertThat(updatedCase.getResponse().isPresent()).isTrue();
        assertThat(updatedCase.getResponse().get()).isEqualTo(response);
        assertThat(updatedCase.getRespondedAt()).isCloseTo(LocalDateTime.now(), within(10, ChronoUnit.SECONDS));
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
