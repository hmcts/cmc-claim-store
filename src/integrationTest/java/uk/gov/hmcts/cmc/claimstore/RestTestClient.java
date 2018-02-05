package uk.gov.hmcts.cmc.claimstore;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

@Component
public class RestTestClient {

    private final TestsSetup testsSetup;
    private final JsonMapper jsonMapper;

    @Autowired
    public RestTestClient(
        TestsSetup testsSetup,
        JsonMapper jsonMapper
    ) {
        this.testsSetup = testsSetup;
        this.jsonMapper = jsonMapper;
    }

    public Response post(ClaimData claimData) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + testsSetup.getUserAuthenticationToken())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + testsSetup.getUserId());
    }

}
