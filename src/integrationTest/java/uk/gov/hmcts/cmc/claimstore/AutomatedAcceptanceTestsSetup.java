package uk.gov.hmcts.cmc.claimstore;

import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.aat.TestInstance;
import uk.gov.hmcts.cmc.claimstore.aat.idam.IdamTestService;
import uk.gov.hmcts.cmc.claimstore.aat.idam.TestUser;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;

import javax.annotation.PostConstruct;

@Primary
@Component
@Profile("aat-tests")
public class AutomatedAcceptanceTestsSetup implements TestsSetup {

    private final IdamTestService idamTestService;
    private final IdamApi idamApi;
    private final TestUser testUser;
    private final TestInstance testInstance;

    private String authenticationToken;
    private String userId;

    @Autowired
    public AutomatedAcceptanceTestsSetup(
        IdamTestService idamTestService,
        IdamApi idamApi,
        TestUser testUser,
        TestInstance testInstance
    ) {
        this.idamTestService = idamTestService;
        this.idamApi = idamApi;
        this.testUser = testUser;
        this.testInstance = testInstance;
    }

    @PostConstruct
    public void initialize() {
        RestAssured.baseURI = testInstance.getUri();
        authenticationToken = "Bearer " + idamTestService.logIn(testUser.getUsername(), testUser.getPassword());
        userId = idamApi.retrieveUserDetails(authenticationToken).getId();
    }

    @Override
    public String getUserAuthenticationToken() {
        return authenticationToken;
    }

    @Override
    public String getUserId() {
        return userId;
    }

}
