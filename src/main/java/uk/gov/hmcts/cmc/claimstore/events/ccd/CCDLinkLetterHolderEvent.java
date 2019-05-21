package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CCDLinkLetterHolderEvent {
    private final Claim claim;
    private final String letterHolderId;
    private final String authorization;

    public CCDLinkLetterHolderEvent(Claim claim, String letterHolderId, String authorization) {
        this.claim = claim;
        this.letterHolderId = letterHolderId;
        this.authorization = authorization;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
