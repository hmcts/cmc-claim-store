package uk.gov.hmcts.cmc.claimstore.events.ccd;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentCollection;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode
@Getter
public class CCDSaveClaimDocumentEvent {

    private String authorisation;
    private Claim claim;
    private ClaimDocumentCollection claimDocumentCollection;
    private ClaimDocumentType claimDocumentType;

    public CCDSaveClaimDocumentEvent(
        String authorisation,
        Claim claim,
        ClaimDocumentCollection claimDocumentCollection,
        ClaimDocumentType claimDocumentType
    ) {
        this.authorisation = authorisation;
        this.claim = claim;
        this.claimDocumentCollection = claimDocumentCollection;
        this.claimDocumentType = claimDocumentType;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
