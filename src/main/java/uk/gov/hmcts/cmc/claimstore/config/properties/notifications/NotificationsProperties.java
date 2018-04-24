package uk.gov.hmcts.cmc.claimstore.config.properties.notifications;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Component
@Validated
@ConfigurationProperties(prefix = "notifications")
public class NotificationsProperties {

    @NotEmpty
    private String govNotifyApiKey;

    @URL
    @NotEmpty
    private String frontendBaseUrl;

    @URL
    @NotEmpty
    private String respondToClaimUrl;

    @Valid
    private NotificationTemplates templates;

    public String getGovNotifyApiKey() {
        return govNotifyApiKey;
    }

    public void setGovNotifyApiKey(String govNotifyApiKey) {
        this.govNotifyApiKey = govNotifyApiKey;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public NotificationTemplates getTemplates() {
        return templates;
    }

    public void setTemplates(NotificationTemplates templates) {
        this.templates = templates;
    }

    public String getRespondToClaimUrl() {
        return respondToClaimUrl;
    }

    public void setRespondToClaimUrl(String respondToClaimUrl) {
        this.respondToClaimUrl = respondToClaimUrl;
    }
}
