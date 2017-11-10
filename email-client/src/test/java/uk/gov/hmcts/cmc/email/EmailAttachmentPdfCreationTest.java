package uk.gov.hmcts.cmc.email;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailAttachmentPdfCreationTest {

    private static final byte[] CONTENT = { 1, 2, 3, 4 };
    private static final String FILE_NAME = "document.pdf";

    @Test
    public void shouldCreateEmailAttachmentWithPdfContentType() {
        EmailAttachment attachment = EmailAttachment.pdf(CONTENT, FILE_NAME);
        assertThat(attachment.getContentType()).isEqualTo("application/pdf");
    }

    @Test
    public void shouldCreateEmailAttachmentWithPdfProvidedContent() throws Exception {
        EmailAttachment attachment = EmailAttachment.pdf(CONTENT, FILE_NAME);
        byte[] content = IOUtils.toByteArray(attachment.getData().getInputStream());
        assertThat(content).isEqualTo(CONTENT);
    }

    @Test
    public void shouldCreateEmailAttachmentWithPdfProvidedFileName() {
        EmailAttachment attachment = EmailAttachment.pdf(CONTENT, FILE_NAME);
        assertThat(attachment.getFilename()).isEqualTo(FILE_NAME);
    }

}
