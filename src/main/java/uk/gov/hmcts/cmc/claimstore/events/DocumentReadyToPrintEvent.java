package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Arrays;
import java.util.List;

public class DocumentReadyToPrintEvent {
    private final Claim claim;
    private List<Document> documents;

    public DocumentReadyToPrintEvent(Claim claim, Document... documents) {

        this.claim = claim;
        this.documents = Arrays.asList(documents);
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public Claim getClaim() {
        return claim;
    }
}
