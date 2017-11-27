package uk.gov.hmcts.cmc.claimstore.documents.output;

public class PDF {
    public static final String CONTENT_TYPE = "application/pdf";
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
