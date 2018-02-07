package uk.gov.hmcts.cmc.claimstore.aat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.JwtHelper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import javax.annotation.PostConstruct;

@Component
public class Bootstrap {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final JwtHelper jwtHelper;
    private final TestUser testUser;
    private final TestInstance testInstance;

    private String authenticationToken;
    private String userId;

    @Autowired
    public Bootstrap(
        ObjectMapper objectMapper,
        UserService userService,
        JwtHelper jwtHelper,
        TestUser testUser,
        TestInstance testInstance
    ) {
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.testUser = testUser;
        this.jwtHelper = jwtHelper;
        this.testInstance = testInstance;
    }

    @PostConstruct
    public void initialize() {
        RestAssured.baseURI = testInstance.getUri();
        RestAssured.config = RestAssured.config()
            .objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper)
            );
        authenticationToken = userService
            .authenticateUser(testUser.getUsername(), testUser.getPassword()).getAuthorisation();
        userId = jwtHelper.getUserId(authenticationToken);
    }

    public String getUserAuthenticationToken() {
        return authenticationToken;
    }

    public String getUserId() {
        return userId;
    }

}
