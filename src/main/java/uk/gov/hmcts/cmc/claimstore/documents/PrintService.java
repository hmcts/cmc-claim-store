package uk.gov.hmcts.cmc.claimstore.documents;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

public interface PrintService {
    void print(Claim claim, Document defendantLetterDocument, Document sealedClaimDocument);
}
