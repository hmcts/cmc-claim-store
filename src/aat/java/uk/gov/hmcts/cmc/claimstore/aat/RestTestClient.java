package uk.gov.hmcts.cmc.claimstore.aat;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

@Component
public class RestTestClient {

    private final Bootstrap bootstrap;
    private final JsonMapper jsonMapper;

    @Autowired
    public RestTestClient(
        Bootstrap bootstrap,
        JsonMapper jsonMapper
    ) {
        this.bootstrap = bootstrap;
        this.jsonMapper = jsonMapper;
    }

    public Response post(ClaimData claimData) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .header(HttpHeaders.AUTHORIZATION, bootstrap.getUserAuthenticationToken())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + bootstrap.getUserId());
    }

}
