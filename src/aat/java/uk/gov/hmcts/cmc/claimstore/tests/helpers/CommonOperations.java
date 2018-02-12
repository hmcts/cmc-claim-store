package uk.gov.hmcts.cmc.claimstore.tests.helpers;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.util.UUID;

@Service
public class CommonOperations {

    private final JsonMapper jsonMapper;

    @Autowired
    public CommonOperations(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public Claim submitClaim(String userAuthentication, String userId) {
        UUID externalId = UUID.randomUUID();

        ClaimData claimData = SampleClaimData.submittedByClaimantBuilder()
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

    public void linkDefendant(String claimExternalId, String userAuthentication, String userId) {
        RestAssured
            .given()
            .header(HttpHeaders.AUTHORIZATION, userAuthentication)
            .put("/claims/" + claimExternalId + "/defendant/" + userId);
    }

}
