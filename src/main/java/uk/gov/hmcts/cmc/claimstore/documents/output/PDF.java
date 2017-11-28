package uk.gov.hmcts.cmc.claimstore.documents.output;

import org.springframework.http.MediaType;

public class PDF {
    public static final String CONTENT_TYPE = MediaType.APPLICATION_PDF_VALUE;
    public static final String EXTENSION = ".pdf";

    private final String filename;
    private final byte[] bytes;

    public PDF(final String filename, final byte[] bytes) {
        this.filename = filename;
        this.bytes = bytes;
    }

    public String getFilename() {
        return filename + EXTENSION;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
