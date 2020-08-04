package uk.gov.hmcts.cmc.claimstore.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Getter
@AllArgsConstructor
public class GeneralLetterReadyToPrintEvent {
    private final Claim claim;
    private final Document generalLetterDocument;
    private final String authorisation;
}
