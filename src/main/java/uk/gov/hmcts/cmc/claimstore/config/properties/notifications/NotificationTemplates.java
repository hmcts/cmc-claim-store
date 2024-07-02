package uk.gov.hmcts.cmc.claimstore.config.properties.notifications;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "notifications.templates")
public class NotificationTemplates {

    @Valid
    private EmailTemplates email;

    public EmailTemplates getEmail() {
        return email;
    }

    public void setEmail(EmailTemplates email) {
        this.email = email;
    }

}
