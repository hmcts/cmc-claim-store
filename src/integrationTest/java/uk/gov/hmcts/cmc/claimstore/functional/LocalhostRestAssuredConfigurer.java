package uk.gov.hmcts.cmc.claimstore.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LocalhostRestAssuredConfigurer {

    private final ObjectMapper objectMapper;

    @Autowired
    public LocalhostRestAssuredConfigurer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void onServletContainerReady(EmbeddedServletContainerInitializedEvent event) {
        RestAssured.port = event.getEmbeddedServletContainer().getPort();
        RestAssured.config = RestAssured.config()
            .objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper)
            );
    }

}
