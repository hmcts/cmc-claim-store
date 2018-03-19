package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Objects;

public class DocumentReadyToPrintEvent {
    private final Claim claim;
    private final Document defendantLetterDocument;
    private final Document sealedClaimDocument;

    public DocumentReadyToPrintEvent(Claim claim, Document defendantLetterDocument, Document sealedClaimDocument) {

        this.claim = claim;
        this.defendantLetterDocument = defendantLetterDocument;
        this.sealedClaimDocument = sealedClaimDocument;
    }

    public Claim getClaim() {
        return claim;
    }

    public Document getDefendantLetterDocument() {
        return defendantLetterDocument;
    }

    public Document getSealedClaimDocument() {
        return sealedClaimDocument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DocumentReadyToPrintEvent that = (DocumentReadyToPrintEvent) obj;
        return Objects.equals(claim, that.claim)
            && Objects.equals(defendantLetterDocument, that.defendantLetterDocument)
            && Objects.equals(sealedClaimDocument, that.sealedClaimDocument);
    }

    @Override
    public int hashCode() {

        return Objects.hash(claim, defendantLetterDocument, sealedClaimDocument);
    }
}
