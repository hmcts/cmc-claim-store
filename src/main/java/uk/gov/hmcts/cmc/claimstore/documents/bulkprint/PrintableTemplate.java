package uk.gov.hmcts.cmc.claimstore.documents.bulkprint;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

@EqualsAndHashCode(callSuper = true)
public class PrintableTemplate extends PrintableDocument {

    public PrintableTemplate(Document document, String fileName) {
        super(document, fileName);
    }

    @Override
    public byte[] getContent(PDFServiceClient pdfServiceClient) {
        return pdfServiceClient.generateFromHtml(
            super.getDocument().template.getBytes(),
            super.getDocument().values);
    }
}
