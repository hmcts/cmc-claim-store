package uk.gov.hmcts.cmc.claimstore.rpa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Component
@Validated
@ConfigurationProperties(prefix = "rpa-notifications")
public class EmailProperties {

    @NotBlank
    private String sender;

    @NotBlank
    @Value("recipient-sealed-claim")
    private String recipient;

    @NotBlank
    @Value("defence-response")
    private String defenceResponseRecipient;

    @NotBlank
    @Value("default-judgement")
    private String defaultJudgementRecipient;

    @NotBlank
    @Value("recipient-more-time")
    private String recipientMoreTimeRecipient;

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

    public String getDefenceResponseRecipient() {
        return defenceResponseRecipient;
    }

    public String getDefaultJudgementRecipient() {
        return defaultJudgementRecipient;
    }

    public String getRecipientMoreTimeRecipient() {
        return recipientMoreTimeRecipient;
    }

    public void setDefenceResponseRecipient(String defenceResponseRecipient) {
        this.defenceResponseRecipient = defenceResponseRecipient;
    }

    public void setDefaultJudgementRecipient(String defaultJudgementRecipient) {
        this.defaultJudgementRecipient = defaultJudgementRecipient;
    }

    public void setRecipientMoreTimeRecipient(String recipientMoreTimeRecipient) {
        this.recipientMoreTimeRecipient = recipientMoreTimeRecipient;
    }
}
