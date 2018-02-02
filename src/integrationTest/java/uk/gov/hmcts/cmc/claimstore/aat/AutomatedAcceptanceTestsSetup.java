package uk.gov.hmcts.cmc.claimstore.aat;

import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.aat.idam.IdamTestService;
import uk.gov.hmcts.cmc.claimstore.idam.IdamApi;

@Component
@Profile("aat-tests")
public class AutomatedAcceptanceTestsSetup implements TestsSetup {

    private final IdamTestService idamTestService;
    private final IdamApi idamApi;

    private String authenticationToken;
    private String userId;

    @Autowired
    public AutomatedAcceptanceTestsSetup(
        IdamTestService idamTestService,
        IdamApi idamApi
    ) {
        this.idamTestService = idamTestService;
        this.idamApi = idamApi;
    }

    @EventListener
    public void onApplicationContextReady(ContextRefreshedEvent event) {
        RestAssured.baseURI = "http://localhost:4400";
        authenticationToken = idamTestService.logIn("r.lewandowski@kainos.com", "Password12");
        userId = idamApi.retrieveUserDetails("Bearer " + authenticationToken).getId();
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
