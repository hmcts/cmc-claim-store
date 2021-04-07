package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
public class BreathingSpaceEvent {
    protected final Claim claim;
    protected final CCDCase ccdCase;
    protected final String authorisation;
    protected final String letterTemplateId;
    protected final String claimantEmailTemplateId;
    protected final String defendantEmailTemplateId;
    protected final boolean enteredByCitizen;

    public BreathingSpaceEvent(
        Claim claim,
        CCDCase ccdCase,
        String authorisation,
        String letterTemplateId,
        String claimantEmailTemplateId,
        String defendantEmailTemplateId,
        boolean enteredByCitizen
    ) {
        this.claim = claim;
        this.ccdCase = ccdCase;
        this.authorisation = authorisation;
        this.letterTemplateId = letterTemplateId;
        this.claimantEmailTemplateId = claimantEmailTemplateId;
        this.defendantEmailTemplateId = defendantEmailTemplateId;
        this.enteredByCitizen = enteredByCitizen;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
