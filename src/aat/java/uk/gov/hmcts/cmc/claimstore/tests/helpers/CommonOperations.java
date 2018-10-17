package uk.gov.hmcts.cmc.claimstore.tests.helpers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.UserRoleRequest;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.offers.Settlement;

import java.util.UUID;

@Service
public class CommonOperations {

    private final JsonMapper jsonMapper;
    private final TestData testData;

    @Autowired
    public CommonOperations(
        JsonMapper jsonMapper,
        TestData testData
    ) {
        this.jsonMapper = jsonMapper;
        this.testData = testData;
    }

    public Claim submitClaim(String userAuthentication, String userId) {
        UUID externalId = UUID.randomUUID();
        return submitClaim(userAuthentication, userId, testData.submittedByClaimantBuilder()
            .withExternalId(externalId)
            .build());
    }

    public Claim submitClaim(String userAuthentication, String userId, ClaimData claimData) {
        submitPrePaymentClaim(claimData.getExternalId().toString(), userAuthentication);
        return saveClaim(claimData, userAuthentication, userId).then().extract().body().as(Claim.class);
    }

    public Response submitPrePaymentClaim(String externalId, String userAuthentication) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .post("/claims/" + externalId + "/pre-payment");
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

    public Response saveUserRoles(UserRoleRequest userRoleRequest, String userAuthentication) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .body(jsonMapper.toJson(userRoleRequest))
            .when()
            .post("/user/roles");
    }

    public Response getUserRole(String userAuthentication) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .get("/user/roles");
    }

    public void linkDefendant(String userAuthentication) {
        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .put("/claims/defendant/link");
    }

    public Claim retrieveClaim(String externalId, String userAuthentication) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .get(String.format("claims/%s", externalId))
            .then()
            .extract()
            .body()
            .as(Claim.class);
    }

    public Response submitResponse(
        uk.gov.hmcts.cmc.domain.models.response.Response response,
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

    public Response signSettlementAgreement(
        String claimExternalId,
        String userAuthentication,
        Settlement settlement
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .body(jsonMapper.toJson(settlement))
            .when()
            .post("/claims/" + claimExternalId + "/settlement");
    }

    public Response submitClaimantResponse(
        ClaimantResponse response,
        String claimExternalId,
        User claimant
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .body(jsonMapper.toJson(response))
            .when()
            .post("/responses/" + claimExternalId + "/claimant/" + claimant.getUserDetails().getId());
    }

    public Response requestCCJ(String externalId, CountyCourtJudgment ccj, boolean issue, User user) {
        String path = "/claims/" + externalId + "/county-court-judgment";
        String issuePath = issue ? path.concat("?issue=true") : path;

        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(ccj))
            .when()
            .post(issuePath);
    }

    public Response paidInFull(String externalId, PaidInFull paidInFull, User user) {
        String path = "/claims/" + externalId + "/paid-in-full";

        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(paidInFull))
            .when()
            .put(path);
    }
}
