package uk.gov.hmcts.cmc.claimstore.events.claim;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.claimstore.events.ClaimCreationEvent;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode(callSuper = true)
@Value
public class CaseworkerHwfClaimIssueEvent extends ClaimCreationEvent {

    public CaseworkerHwfClaimIssueEvent(
        Claim claim,
        String submitterName,
        String authorisation
    ) {
        super(claim, submitterName, authorisation);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
