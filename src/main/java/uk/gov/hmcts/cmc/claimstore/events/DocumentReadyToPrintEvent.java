package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Objects;

public class DocumentReadyToPrintEvent {
    private final Claim claim;
    private final Document defendantLetterDocument;
    private final Document sealedClaimDocument;
    private final Document claimantRefusedDefendantDocument;
    private final String authorisation;

    public DocumentReadyToPrintEvent(
        Claim claim,
        Document defendantLetterDocument,
        Document sealedClaimDocument,
        Document claimantRefusedDefendantDocument,
        String authorisation
    ) {
        this.claim = claim;
        this.defendantLetterDocument = defendantLetterDocument;
        this.sealedClaimDocument = sealedClaimDocument;
        this.claimantRefusedDefendantDocument = claimantRefusedDefendantDocument;
        this.authorisation = authorisation;
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

    public Document getClaimantRefusedDefendantDocument() {
        return claimantRefusedDefendantDocument;
    }

    public String getAuthorisation() {
        return authorisation;
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
            && Objects.equals(sealedClaimDocument, that.sealedClaimDocument)
            && Objects.equals(authorisation, that.authorisation);
    }

    @Override
    public int hashCode() {

        return Objects.hash(claim, defendantLetterDocument, sealedClaimDocument, authorisation);
    }
}
