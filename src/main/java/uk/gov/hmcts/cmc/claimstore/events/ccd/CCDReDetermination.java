package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReDetermination;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@Getter
public class CCDReDetermination {
    private final Claim claim;
    private final String authorisation;
    private final ReDetermination redetermination;

    public CCDReDetermination(Claim claim, String authorisation, ReDetermination redetermination) {

        this.claim = claim;
        this.authorisation = authorisation;
        this.redetermination = redetermination;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
