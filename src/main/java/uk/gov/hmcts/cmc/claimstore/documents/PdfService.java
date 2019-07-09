package uk.gov.hmcts.cmc.claimstore.documents;

import uk.gov.hmcts.cmc.domain.models.Claim;

public interface PdfService {
    byte[] createPdf(Claim claim);

    String filename(Claim claim);
}
