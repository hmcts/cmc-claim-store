package uk.gov.hmcts.cmc.claimstore.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

import static uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim.USER_ID;

@Component
public class RestTestClient {

    private final JsonMapper jsonMapper;
    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    @Autowired
    public RestTestClient(
        JsonMapper jsonMapper,
        ObjectMapper objectMapper,
        MockMvc mockMvc
    ) {
        this.jsonMapper = jsonMapper;
        this.objectMapper = objectMapper;
        this.mockMvc = mockMvc;
        configure();
    }

    private void configure() {
        RestAssuredMockMvc.config = RestAssuredMockMvc
            .config()
            .objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper)
            );
        RestAssuredMockMvc.mockMvc(mockMvc);
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
