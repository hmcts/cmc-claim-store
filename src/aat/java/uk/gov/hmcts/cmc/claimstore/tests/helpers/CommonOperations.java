package uk.gov.hmcts.cmc.claimstore.tests.helpers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.tests.Bootstrap;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class CommonOperations {
    private static final Pattern jsonListPattern = Pattern.compile("^\\[.*\\]$");

    private final JsonMapper jsonMapper;
    private final TestData testData;
    private final Bootstrap bootstrap;

    @Autowired
    public CommonOperations(
        JsonMapper jsonMapper,
        TestData testData,
        Bootstrap bootstrap
    ) {
        this.jsonMapper = jsonMapper;
        this.testData = testData;
        this.bootstrap = bootstrap;
    }

    public Claim submitClaim(String userAuthentication, String userId) {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = testData.submittedByClaimantBuilder()
            .withExternalId(externalId)
            .build();

        return saveClaim(claimData, userAuthentication, userId).then().extract().body().as(Claim.class);
    }

    private Response saveClaim(ClaimData claimData, String userAuthentication, String userId) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + userId);
    }

    public void testCasesRetrievalFor(String uriPath) {
        String response = RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, bootstrap.getSmokeTestCitizen().getAuthorisation())
            .when()
            .get(uriPath)
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        assertThat(response).matches(jsonListPattern);
    }

    public void linkDefendant(String userAuthentication) {
        RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .put("/claims/defendant/link");
    }

    public Response submitResponse(
        uk.gov.hmcts.cmc.domain.models.Response response,
        String claimExternalId,
        User defendant
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .body(jsonMapper.toJson(response))
            .when()
            .post("/responses/claim/" + claimExternalId + "/defendant/" + defendant.getUserDetails().getId());
    }

    public Response submitOffer(
        Offer offer,
        String claimExternalId,
        String userAuthentication,
        MadeBy madeBy
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .body(jsonMapper.toJson(offer))
            .when()
            .post("/claims/" + claimExternalId + "/offers/" + madeBy.name());
    }

    public Response acceptOffer(
        String claimExternalId,
        String userAuthentication,
        MadeBy madeBy
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .post("/claims/" + claimExternalId + "/offers/" + madeBy.name() + "/accept");
    }

    public Response rejectOffer(
        String claimExternalId,
        String userAuthentication,
        MadeBy madeBy
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .post("/claims/" + claimExternalId + "/offers/" + madeBy.name() + "/reject");
    }

    public Response countersignOffer(
        String claimExternalId,
        String userAuthentication,
        MadeBy madeBy
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .post("/claims/" + claimExternalId + "/offers/" + madeBy.name() + "/countersign");
    }
}
