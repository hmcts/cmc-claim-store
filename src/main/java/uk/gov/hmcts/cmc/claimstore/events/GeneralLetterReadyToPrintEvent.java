package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Objects;

public class GeneralLetterReadyToPrintEvent {
    private final Claim claim;
    private final Document generalLetterDocument;

    public GeneralLetterReadyToPrintEvent(Claim claim, Document generalLetterDocument) {
        this.claim = claim;
        this.generalLetterDocument = generalLetterDocument;
    }

    public Claim getClaim() {
        return claim;
    }

    public Document getGeneralLetterDocument() {
        return generalLetterDocument;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GeneralLetterReadyToPrintEvent that = (GeneralLetterReadyToPrintEvent) obj;
        return Objects.equals(claim, that.claim)
            && Objects.equals(generalLetterDocument, that.generalLetterDocument);
    }

    @Override
    public int hashCode() {

        return Objects.hash(claim, generalLetterDocument);
    }
}
