package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.domain.models.Claim;

public class SealedClaimGeneratedEvent {
    private final Claim claim;
    private final String authorization;
    private final byte[] document;

    public SealedClaimGeneratedEvent(final Claim claim, String authorization, final byte[] document) {
        this.claim = claim;
        this.document = document;
        this.authorization = authorization;
    }

    public Claim getClaim() {
        return claim;
    }

    public String getAuthorisation() {
        return authorization;
    }

    public byte[] getDocument() {
        return document;
    }
}
