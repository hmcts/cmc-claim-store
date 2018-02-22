package uk.gov.hmcts.cmc.claimstore.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;

import javax.annotation.PostConstruct;

@Component
public class Bootstrap {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final AATConfiguration aatConfiguration;

    private User smokeTestCitizen;

    @Autowired
    public Bootstrap(
        ObjectMapper objectMapper,
        UserService userService,
        AATConfiguration aatConfiguration
    ) {
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.aatConfiguration = aatConfiguration;
    }

    @PostConstruct
    public void initialize() {
        RestAssured.baseURI = aatConfiguration.getTestInstanceUri();
        RestAssured.config = RestAssured.config()
            .objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper)
            );
        RestAssured.useRelaxedHTTPSValidation();
        smokeTestCitizen = userService.authenticateUser(
            aatConfiguration.getSmokeTestCitizen().getUsername(),
            aatConfiguration.getSmokeTestCitizen().getPassword()
        );
    }

    public User getSmokeTestCitizen() {
        return smokeTestCitizen;
    }

}
