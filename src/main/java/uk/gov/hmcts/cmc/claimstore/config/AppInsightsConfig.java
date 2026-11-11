package uk.gov.hmcts.cmc.claimstore.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.logs.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppInsightsConfig {

    @Bean
    public Logger openTelemetryLogger() {
        return GlobalOpenTelemetry.get().getLogsBridge().get("uk.gov.hmcts.cmc.claimstore");
    }
}
