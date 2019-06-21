package uk.gov.hmcts.cmc.claimstore.documents.bulkprint;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.Base64;

@EqualsAndHashCode(callSuper = true)
public class PrintablePdf extends PrintableDocument {

    public PrintablePdf(Document document, String fileName) {
        super(document, fileName);
    }

    @Override
    public byte[] getContent(PDFServiceClient pdfServiceClient) {
        return Base64.getDecoder().decode(getDocument().template.getBytes());
    }
}
