package uk.gov.hmcts.cmc.claimstore.events;

import lombok.Value;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Value
public class DocumentUploadEvent {
    Claim claim;
    String authorization;
    List<PDF> documents;

    public DocumentUploadEvent(Claim claim, String authorization, PDF... documents) {
        this.claim = claim;
        this.authorization = authorization;
        this.documents = newArrayList(documents);
    }

}
