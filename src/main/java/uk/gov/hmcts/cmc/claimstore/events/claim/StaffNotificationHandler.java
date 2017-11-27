package uk.gov.hmcts.cmc.claimstore.events.claim;

import uk.gov.hmcts.cmc.claimstore.events.solicitor.RepresentedClaimIssuedEvent;

public interface StaffNotificationHandler {
    void onClaimIssued(final ClaimIssuedEvent event);

    void onRepresentedClaimIssued(final RepresentedClaimIssuedEvent event);
}
