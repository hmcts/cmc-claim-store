package uk.gov.hmcts.cmc.claimstore.documents;

import uk.gov.hmcts.cmc.claimstore.documents.bulkprint.Printable;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;

import java.util.List;

public interface PrintService {
    BulkPrintDetails printHtmlLetter(Claim claim, List<Printable> documents,
                                     BulkPrintRequestType letterType, String authorisation);

    BulkPrintDetails printPdf(Claim claim, List<Printable> documents,
                              BulkPrintRequestType letterType, String authorisation);
}
