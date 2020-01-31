package uk.gov.hmcts.cmc.claimstore.events;

import lombok.Data;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Data
public class DocumentReadyToPrintEvent {
    private final Claim claim;
    private final Document defendantLetterDocument;
    private final Document sealedClaimDocument;
    private final String authorisation;
}
