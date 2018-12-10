package uk.gov.hmcts.cmc.claimstore.events.claimantresponse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@Getter
public class RejectOrganisationPaymentPlanEvent {

    private final Claim claim;

    public RejectOrganisationPaymentPlanEvent(Claim claim) {
        this.claim = claim;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
