package uk.gov.hmcts.cmc.email;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailConfiguration {

    @Bean
    public AppInsightsService appInsightsService(TelemetryClient telemetry) {
        return new AppInsightsService(telemetry);
    }

    @Bean
    public EmailService emailService(AppInsightsService appInsightsService, JavaMailSender sender) {
        return new EmailService(appInsightsService, sender);
    }
}
