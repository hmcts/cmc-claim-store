package uk.gov.hmcts.cmc.claimstore.events.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Getter
@AllArgsConstructor
public class PaperDefenceReadyToPrintEvent {
    private final Claim claim;
    private final Document coverLetter;
    private final Document oconForm;
    private final String authorisation;
}

