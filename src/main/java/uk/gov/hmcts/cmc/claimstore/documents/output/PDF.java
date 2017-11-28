package uk.gov.hmcts.cmc.claimstore.documents.output;

import org.springframework.http.MediaType;

public class PDF {
    public static final String CONTENT_TYPE = MediaType.APPLICATION_PDF_VALUE;
    public static final String EXTENSION = ".pdf";

    private final String fileBaseName;
    private final byte[] bytes;

    public PDF(final String fileBaseName, final byte[] bytes) {
        this.fileBaseName = fileBaseName;
        this.bytes = bytes;
    }

    public String getFilename() {
        return fileBaseName + EXTENSION;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
