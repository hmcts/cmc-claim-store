package uk.gov.hmcts.cmc.claimstore.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.util.List;

@Getter
@AllArgsConstructor
public class BulkPrintTransferEvent {
    private final Claim claim;
    private final Document coverLetter;
    private final List<PrintableDocument> caseDocuments;

    public static class PrintableDocument {
        private final Document document;
        private final String fileName;

        public PrintableDocument(Document document, String fileName) {
            this.document = document;
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public Document getDocument() {
            return document;
        }
    }
}
