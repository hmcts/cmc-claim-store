package uk.gov.hmcts.cmc.claimstore.events;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Builder
@Getter
public class GeneralLetterReadyToPrintEvent {
    private final Claim claim;
    private final Document generalLetterDocument;

    public GeneralLetterReadyToPrintEvent(Claim claim, Document generalLetterDocument) {
        this.claim = claim;
        this.generalLetterDocument = generalLetterDocument;
    }
}
