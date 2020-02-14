package uk.gov.hmcts.cmc.claimstore.tests.helpers;

import com.google.common.collect.ImmutableList;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.PaidInFull;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;
import uk.gov.hmcts.cmc.domain.models.UserRoleRequest;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ClaimantResponse;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;
import uk.gov.hmcts.cmc.domain.models.offers.Offer;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.UUID;

import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.ADMISSIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.DQ_FLAG;
import static uk.gov.hmcts.cmc.domain.models.ClaimFeatures.LA_PILOT_FLAG;

@Service
public class CommonOperations {

    private final JsonMapper jsonMapper;
    private final TestData testData;
    private final ClaimOperation claimOperation;

    @Autowired
    public CommonOperations(
        JsonMapper jsonMapper,
        TestData testData,
        ClaimOperation claimOperation
    ) {
        this.jsonMapper = jsonMapper;
        this.testData = testData;
        this.claimOperation = claimOperation;
    }

    @LogExecutionTime
    public Claim submitClaimWithDefendantCollectionId(String userAuthentication, String userId, String collectionId) {
        UUID externalId = UUID.randomUUID();
        return submitClaim(userAuthentication, userId, testData.submittedByClaimantBuilder()
            .withDefendant(SampleTheirDetails.builder().withCollectionId(collectionId).individualDetails())
            .withExternalId(externalId)
            .build());
    }

    @LogExecutionTime
    public Claim submitClaim(String userAuthentication, String userId) {
        return submitClaim(
            userAuthentication,
            userId,
            testData.submittedByClaimantBuilder().withExternalId(UUID.randomUUID()).build()
        );
    }

    @LogExecutionTime
    public Claim submitClaim(String userAuthentication, String userId, ClaimData claimData) {
        Claim claim = saveClaim(claimData, userAuthentication, userId).then().extract().body().as(Claim.class);
        return claimOperation.getClaimWithLetterHolder(claim.getExternalId(), userAuthentication);
    }

    @LogExecutionTime
    public Response saveClaim(ClaimData claimData, String userAuthentication, String userId) {

        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .header("Features",
                ImmutableList.of(ADMISSIONS.getValue(), "issuedOn", LA_PILOT_FLAG.getValue(), DQ_FLAG.getValue()))
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + userId);
    }

    @LogExecutionTime
    public Response saveUserRoles(UserRoleRequest userRoleRequest, String userAuthentication) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .body(jsonMapper.toJson(userRoleRequest))
            .when()
            .post("/user/roles");
    }

    @LogExecutionTime
    public Response getUserRole(String userAuthentication) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .get("/user/roles");
    }

    @LogExecutionTime
    public void linkDefendant(String userAuthentication) {
        RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .when()
            .put("/claims/defendant/link");
    }

    @LogExecutionTime
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

    @LogExecutionTime
    public Response submitResponse(
        uk.gov.hmcts.cmc.domain.models.response.Response response,
        String claimExternalId,
        User defendant
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, defendant.getAuthorisation())
            .body(jsonMapper.toJson(response))
            .when()
            .post("/responses/claim/" + claimExternalId + "/defendant/" + defendant.getUserDetails().getId());
    }

    @LogExecutionTime
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

    @LogExecutionTime
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

    @LogExecutionTime
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

    @LogExecutionTime
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

    @LogExecutionTime
    public Response submitClaimantResponse(
        ClaimantResponse response,
        String claimExternalId,
        User claimant
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .body(jsonMapper.toJson(response))
            .when()
            .post("/responses/" + claimExternalId + "/claimant/" + claimant.getUserDetails().getId());
    }

    @LogExecutionTime
    public Response requestCCJ(String externalId, CountyCourtJudgment ccj, User user) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(ccj))
            .when()
            .post("/claims/" + externalId + "/county-court-judgment");
    }

    @LogExecutionTime
    public Response submitReDetermination(
        ReDetermination reDetermination,
        String claimExternalId,
        User claimant
    ) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, claimant.getAuthorisation())
            .body(jsonMapper.toJson(reDetermination))
            .when()
            .post("/claims/" + claimExternalId + "/re-determination");
    }

    @LogExecutionTime
    public Response paidInFull(String externalId, PaidInFull paidInFull, User user) {
        String path = "/claims/" + externalId + "/paid-in-full";

        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, user.getAuthorisation())
            .body(jsonMapper.toJson(paidInFull))
            .when()
            .put(path);
    }
}
