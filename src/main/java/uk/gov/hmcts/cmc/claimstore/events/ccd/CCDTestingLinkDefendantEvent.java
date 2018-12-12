package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CCDTestingLinkDefendantEvent {
    private final String claimReferenceNumber;
    private final String defendantId;

    public CCDTestingLinkDefendantEvent(String claimReferenceNumber, String defendantId) {

        this.claimReferenceNumber = claimReferenceNumber;
        this.defendantId = defendantId;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
