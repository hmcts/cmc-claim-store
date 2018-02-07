package uk.gov.hmcts.cmc.claimstore.tests;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.services.UserHelper;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import javax.annotation.PostConstruct;

@Component
public class Bootstrap {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final UserHelper userHelper;
    private final TestUser testUser;
    private final TestInstance testInstance;

    private String authenticationToken;
    private String userId;

    @Autowired
    public Bootstrap(
        ObjectMapper objectMapper,
        UserService userService,
        UserHelper userHelper,
        TestUser testUser,
        TestInstance testInstance
    ) {
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.testUser = testUser;
        this.userHelper = userHelper;
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
        userId = decodeUserId(authenticationToken);
    }

    public String getUserAuthenticationToken() {
        return authenticationToken;
    }

    public String getUserId() {
        return userId;
    }

    private String decodeUserId(String authorisation) {
        DecodedJWT decodedToken = JWT.decode(authorisation);
        return decodedToken.getClaims().get("id").asString();
    }

}
