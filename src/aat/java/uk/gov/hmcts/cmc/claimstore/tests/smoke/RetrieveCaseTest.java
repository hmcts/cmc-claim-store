package uk.gov.hmcts.cmc.claimstore.tests.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class RetrieveCaseTest extends BaseTest {

    private static final Pattern jsonListPattern = Pattern.compile("^\\[.*]$");

    @Test
    public void shouldBeAbleToRetrieveCasesBySubmitterId() {
        User citizen = bootstrap.getSmokeTestCitizen();
        testCasesRetrievalFor("/claims/claimant/" + citizen.getUserDetails().getId(),
            citizen.getAuthorisation());
    }

    @Test
    public void shouldBeAbleToRetrieveCasesByDefendantId() {
        User citizen = bootstrap.getSmokeTestCitizen();
        testCasesRetrievalFor("/claims/defendant/" + citizen.getUserDetails().getId(),
            citizen.getAuthorisation());
    }

    private void testCasesRetrievalFor(String uriPath, String authorisation) {
        String response = RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, authorisation)
            .when()
            .get(uriPath)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        assertThat(response).matches(jsonListPattern);
    }
}
