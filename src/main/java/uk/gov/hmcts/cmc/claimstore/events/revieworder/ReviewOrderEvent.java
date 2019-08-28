package uk.gov.hmcts.cmc.claimstore.events.revieworder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class ReviewOrderEvent {

    private final String authorisation;
    private final Claim claim;

    public ReviewOrderEvent(String authorisation, Claim claim) {
        this.claim = claim;
        this.authorisation = authorisation;
    }

    public String getAuthorisation() {
        return this.authorisation;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}

