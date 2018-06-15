package uk.gov.hmcts.cmc.claimstore.rpa.config;

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
    private String sealedClaimRecipient;

    @NotBlank
    private String defenceResponseRecipient;

    @NotBlank
    private String defaultJudgementRecipient;

    @NotBlank
    private String moreTimeRequestedRecipient;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSealedClaimRecipient() {
        return sealedClaimRecipient;
    }

    public void setSealedClaimRecipient(String sealedClaimRecipient) {
        this.sealedClaimRecipient = sealedClaimRecipient;
    }

    public String getDefenceResponseRecipient() {
        return defenceResponseRecipient;
    }

    public void setDefenceResponseRecipient(String defenceResponseRecipient) {
        this.defenceResponseRecipient = defenceResponseRecipient;
    }

    public String getDefaultJudgementRecipient() {
        return defaultJudgementRecipient;
    }

    public void setDefaultJudgementRecipient(String defaultJudgementRecipient) {
        this.defaultJudgementRecipient = defaultJudgementRecipient;
    }

    public String getMoreTimeRequestedRecipient() {
        return moreTimeRequestedRecipient;
    }

    public void setMoreTimeRequestedRecipient(String moreTimeRequestedRecipient) {
        this.moreTimeRequestedRecipient = moreTimeRequestedRecipient;
    }
}
