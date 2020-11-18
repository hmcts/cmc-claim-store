package uk.gov.hmcts.cmc.claimstore.events.legaladvisor;

import lombok.Value;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Value
public class DirectionsOrderReadyToPrintEvent {
    private final Claim claim;
    private final Document coverSheet;
    private final Document directionsOrder;
    private final String authorisation;
}
