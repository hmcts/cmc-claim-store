package uk.gov.hmcts.cmc.claimstore.events.revieworder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class ReviewOrderEvent {

    private final Claim claim;

    public ReviewOrderEvent(Claim claim) {
        this.claim = claim;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

