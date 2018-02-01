package uk.gov.hmcts.cmc.claimstore.functional;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@Component
public class RestTestClient {

    private final JsonMapper jsonMapper;

    @Autowired
    public RestTestClient(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    private static final String AUTHORISATION_TOKEN = "Bearer token";

    public MockMvcResponse post(ClaimData claimData) {
        return RestAssuredMockMvc
            .given()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .body(jsonMapper.toJson(claimData))
            .when()
                .post("/claims/" + USER_ID);
    }

}
