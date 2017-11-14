package uk.gov.hmcts.cmc.claimstore.config.properties.emails;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "staff-notifications")
public class StaffEmailProperties {

    @NotBlank
    private String sender;
    @NotBlank
    private String recipient;

    private final EmailContentTemplates emailTemplates;

    @Autowired
    public StaffEmailProperties(EmailContentTemplates emailTemplates) {
        this.emailTemplates = emailTemplates;
    }

    public EmailContentTemplates getEmailTemplates() {
        return emailTemplates;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

}
