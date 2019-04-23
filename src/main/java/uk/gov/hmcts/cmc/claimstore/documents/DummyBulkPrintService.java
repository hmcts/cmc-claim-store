package uk.gov.hmcts.cmc.claimstore.documents;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

@Service("bulkPrintService")
@ConditionalOnProperty(prefix = "send-letter", name = "url", havingValue = "false")
public class DummyBulkPrintService implements PrintService {

    @Override
    public void print(Claim claim, Document defendantLetterDocument, Document sealedClaimDocument) {
        // No operation need to be performed
    }
}
