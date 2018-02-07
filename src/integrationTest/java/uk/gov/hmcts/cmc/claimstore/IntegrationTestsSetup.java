package uk.gov.hmcts.cmc.claimstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

@Component
public class IntegrationTestsSetup implements TestsSetup {

    private final ObjectMapper objectMapper;

    @Autowired
    public IntegrationTestsSetup(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void onServletContainerReady(EmbeddedServletContainerInitializedEvent event) {
        System.out.println(">>> IntegrationTestsSetup : AppContext " + event.getApplicationContext());
        System.out.println(">>> IntegrationTestsSetup : AppContext hash " + event.getApplicationContext().hashCode());
        System.out.println(">>> IntegrationTestsSetup : Port " + event.getEmbeddedServletContainer().getPort());
        RestAssured.port = event.getEmbeddedServletContainer().getPort();
        RestAssured.config = RestAssured.config()
            .objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper)
            );
    }

    @Override
    public String getUserAuthenticationToken() {
        return BaseSaveTest.AUTHORISATION_TOKEN;
    }

    @Override
    public String getUserId() {
        return SampleClaim.USER_ID;
    }

}
