package uk.gov.hmcts.cmc.claimstore.documents;

import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.domain.models.Claim;

public interface PdfService {
    PDF createPdf(Claim claim);
}
