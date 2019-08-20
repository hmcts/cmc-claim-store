package uk.gov.hmcts.cmc.claimstore.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.idam.IdamTestService;

import javax.annotation.PostConstruct;

@Component
public class Bootstrap {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final AATConfiguration aatConfiguration;
    private final IdamTestService idamTestService;

    private User smokeTestCitizen;
    private User claimant;
    private User defendant;
    private User solicitor;

    @Autowired
    public Bootstrap(
        ObjectMapper objectMapper,
        UserService userService,
        AATConfiguration aatConfiguration,
        IdamTestService idamTestService
    ) {
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.aatConfiguration = aatConfiguration;
        this.idamTestService = idamTestService;
    }

    @PostConstruct
    public void initialize() {
        RestAssured.baseURI = aatConfiguration.getTestInstanceUri();
        RestAssured.config = RestAssured.config()
            .objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper)
            );
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(), new ErrorLoggingFilter());
        smokeTestCitizen = userService.authenticateUser(
            aatConfiguration.getSmokeTestCitizen().getUsername(),
            aatConfiguration.getSmokeTestCitizen().getPassword()
        );
    }

    public User getSmokeTestCitizen() {
        return smokeTestCitizen;
    }

    public User getClaimant() {
        if (claimant == null) {
            claimant = idamTestService.createCitizen();
        }
        return claimant;
    }

    public User getSolicitor() {
        if (solicitor == null) {
            solicitor = idamTestService.createSolicitor();
        }
        return solicitor;
    }

    public User getDefendant() {
        if (defendant == null) {
            defendant = idamTestService.createCitizen();
        }
        return defendant;
    }
}
