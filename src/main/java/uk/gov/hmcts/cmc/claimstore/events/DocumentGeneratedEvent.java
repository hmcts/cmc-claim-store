package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class DocumentGeneratedEvent {
    private final Claim claim;
    private final String authorization;
    private final List<PDF> documents;

    public DocumentGeneratedEvent(Claim claim, String authorization, PDF... documents) {
        this.claim = claim;
        this.authorization = authorization;
        this.documents = newArrayList(documents);
    }

    public Claim getClaim() {
        return claim;
    }

    public String getAuthorisation() {
        return authorization;
    }

    public List<PDF> getDocuments() {
        return documents;
    }
}
