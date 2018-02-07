package uk.gov.hmcts.cmc.claimstore.tests.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.cmc.claimstore.tests.BaseTest;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class RetrieveCaseTest extends BaseTest {

    private static final Pattern jsonListPattern = Pattern.compile("^\\[.*\\]$");

    @Test
    public void shouldBeAbleToRetrieveCasesBySubmitterId() {
        testCasesRetrievalFor("/claims/claimant/" + bootstrap.getUserId());
    }

    @Test
    public void shouldBeAbleToRetrieveCasesByDefendantId() {
        testCasesRetrievalFor("/claims/defendant/" + bootstrap.getUserId());
    }

    private void testCasesRetrievalFor(String uriPath) {
        String response = RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, bootstrap.getUserAuthenticationToken())
            .when()
            .get(uriPath)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();
        assertThat(response).matches(jsonListPattern);
    }

}
