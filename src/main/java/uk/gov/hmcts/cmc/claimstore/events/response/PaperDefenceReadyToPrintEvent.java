package uk.gov.hmcts.cmc.claimstore.events.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Getter
@AllArgsConstructor
public class PaperDefenceReadyToPrintEvent {
    private final Claim claim;
    private final CCDDocument coverLetter;
    private final CCDDocument oconForm;
}

