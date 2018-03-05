package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.List;

import static java.util.Arrays.asList;

public class DocumentReadyToPrintEvent {
    private List<Document> documents;

    public DocumentReadyToPrintEvent(Document... documents) {
        this.documents = asList(documents);
    }
}
