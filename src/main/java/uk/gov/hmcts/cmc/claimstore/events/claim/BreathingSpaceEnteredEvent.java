package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
public class BreathingSpaceEnteredEvent {
    protected final Claim claim;
    protected final CCDCase ccdCase;
    protected final String authorisation;
    protected final String letterTemplateId;
    protected final String claimantEmailTemplateId;
    protected final String defendantEmailTemplateId;

    public BreathingSpaceEnteredEvent(
        Claim claim,
        CCDCase ccdCase,
        String authorisation,
        String letterTemplateId,
        String claimantEmailTemplateId,
        String defendantEmailTemplateId
    ) {
        this.claim = claim;
        this.ccdCase = ccdCase;
        this.authorisation = authorisation;
        this.letterTemplateId = letterTemplateId;
        this.claimantEmailTemplateId = claimantEmailTemplateId;
        this.defendantEmailTemplateId = defendantEmailTemplateId;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
