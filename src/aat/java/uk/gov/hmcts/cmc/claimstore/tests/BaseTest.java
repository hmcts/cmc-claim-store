package uk.gov.hmcts.cmc.claimstore.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.tests.idam.IdamTestService;
import uk.gov.hmcts.cmc.domain.models.ClaimData;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("/environment.properties")
@ActiveProfiles({
    "aat",
    "mocked-database-tests"
})
public abstract class BaseTest {

    @Autowired
    protected Bootstrap bootstrap;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected IdamTestService idamTestService;

    protected Response saveClaim(ClaimData claimData) {
        return RestAssured
            .given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, bootstrap.getUserAuthenticationToken())
            .body(jsonMapper.toJson(claimData))
            .when()
            .post("/claims/" + bootstrap.getUserId());
    }

}
