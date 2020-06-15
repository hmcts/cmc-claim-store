package uk.gov.hmcts.cmc.claimstore.documents;

import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.List;

public interface PrintService {
    void print(Claim claim, List<Printable> documents, String authorisation);

    void printPdf(Claim claim, List<Printable> documents, BulkPrintRequestType letterType, String authorisation);
}
