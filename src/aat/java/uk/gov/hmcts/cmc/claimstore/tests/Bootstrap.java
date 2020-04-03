package uk.gov.hmcts.cmc.claimstore.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.idam.models.User;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.tests.exception.ForbiddenException;
import uk.gov.hmcts.cmc.claimstore.tests.idam.IdamTestService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class Bootstrap {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

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

        authenticateUser();
    }

    private void authenticateUser() {
        try {
            smokeTestCitizen = userService.authenticateUser(
                aatConfiguration.getSmokeTestCitizen().getUsername(),
                aatConfiguration.getSmokeTestCitizen().getPassword()
            );
        } catch (ForbiddenException ex) {
            logger.warn("Ignoring 403 - already exists");
        }
    }

    @PreDestroy
    public void after() {
        try {
            if (claimant != null) {
                idamTestService.deleteUser(claimant.getUserDetails().getEmail());
            }
            if (defendant != null) {
                idamTestService.deleteUser(defendant.getUserDetails().getEmail());
            }
            if (solicitor != null) {
                idamTestService.deleteUser(solicitor.getUserDetails().getEmail());
            }
        } catch (Exception ex) {
            logger.warn("Ignoring exception while trying to delete the user");
        }
    }

    public User getSmokeTestCitizen() {
        return smokeTestCitizen;
    }

    public User getClaimant() {
        synchronized (Bootstrap.class) {
            if (claimant == null) {
                claimant = idamTestService.createCitizen();
            }
        }
        return claimant;
    }

    public User getSolicitor() {
        synchronized (Bootstrap.class) {
            if (solicitor == null) {
                solicitor = idamTestService.createSolicitor();
            }
        }
        return solicitor;
    }

    public User getDefendant() {
        synchronized (Bootstrap.class) {
            if (defendant == null) {
                defendant = idamTestService.createCitizen();
            }
        }
        return defendant;
    }
}
