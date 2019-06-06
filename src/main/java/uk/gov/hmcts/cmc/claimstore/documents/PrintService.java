package uk.gov.hmcts.cmc.claimstore.documents;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Map;

public interface PrintService {
    void print(Claim claim, Map<ClaimDocumentType, Document> documents);
}
