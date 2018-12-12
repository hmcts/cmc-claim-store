package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.net.URI;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Getter
@EqualsAndHashCode
public class CCDLinkSealedClaimDocumentEvent {
    private final String authorization;
    private final Claim claim;
    private final URI sealedClaimDocument;

    public CCDLinkSealedClaimDocumentEvent(String authorization, Claim claim, URI sealedClaimDocument) {

        this.authorization = authorization;
        this.claim = claim;
        this.sealedClaimDocument = sealedClaimDocument;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
