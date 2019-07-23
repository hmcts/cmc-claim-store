package uk.gov.hmcts.cmc.claimstore.documents.bulkprint;

import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

public interface Printable {
    byte[] getContent(PDFServiceClient pdfServiceClient);

    Document getDocument();

    String getFileName();
}
