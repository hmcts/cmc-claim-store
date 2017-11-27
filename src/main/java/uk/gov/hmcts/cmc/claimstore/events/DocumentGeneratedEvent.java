package uk.gov.hmcts.cmc.claimstore.events;

import uk.gov.hmcts.cmc.claimstore.documents.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class DocumentGeneratedEvent {
    private final Claim claim;
    private final String authorization;
    private final List<PDF> documents;

    public DocumentGeneratedEvent(final Claim claim, String authorization, PDF... documents) {
        this(claim, authorization, newArrayList(documents));
    }

    public DocumentGeneratedEvent(final Claim claim, String authorization, final List<PDF> documents) {
        this.claim = claim;
        this.documents = documents;
        this.authorization = authorization;
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
