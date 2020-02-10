package uk.gov.hmcts.cmc.claimstore.documents.bulkprint;

import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.pdf.service.client.PDFServiceClient;
import uk.gov.hmcts.reform.sendletter.api.Document;

@EqualsAndHashCode
public abstract class PrintableDocument implements Printable {
    private static final String EXTENSION = ".pdf";
    private final Document document;
    private final String fileName;

    public PrintableDocument(Document document, String fileName) {
        this.document = document;
        this.fileName = fileName + EXTENSION;
    }

    @Override
    public abstract byte[] getContent(PDFServiceClient pdfServiceClient);

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
