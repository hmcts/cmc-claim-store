package uk.gov.hmcts.cmc.claimstore.config.properties.emails;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Component
@Validated
@ConfigurationProperties(prefix = "staff-notifications")
public class StaffEmailProperties {

    @NotBlank
    private String sender;
    @NotBlank
    private String recipient;
    @NotBlank
    private String legalRecipient;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getLegalRecipient() {
        return legalRecipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setLegalRecipient(String legalRecipient) {
        this.legalRecipient = legalRecipient;
    }

}
