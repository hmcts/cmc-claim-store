package uk.gov.hmcts.cmc.claimstore.documents.output;

import org.springframework.http.MediaType;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

public class PDF {
    public static final String CONTENT_TYPE = MediaType.APPLICATION_PDF_VALUE;
    public static final String EXTENSION = ".pdf";

    private final String fileBaseName;
    private final byte[] bytes;
    private final ClaimDocumentType claimDocumentType;

    public PDF(String fileBaseName, byte[] bytes, ClaimDocumentType claimDocumentType) {
        this.fileBaseName = fileBaseName;
        this.bytes = bytes;
        this.claimDocumentType = claimDocumentType;
    }

    public String getFilename() {
        return fileBaseName + EXTENSION;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public ClaimDocumentType getClaimDocumentType() {
        return this.claimDocumentType;
    }
}
