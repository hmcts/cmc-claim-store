package uk.gov.hmcts.cmc.email;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmailAttachmentTest {

    private static final ByteArrayResource CONTENT = new ByteArrayResource(new byte[] { 1, 2, 3, 4 });
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String CSV_CONTENT_TYPE = "text/csv";
    private static final String PDF_FILE_NAME = "document.pdf";
    private static final String CSV_FILE_NAME = "document.csv";
    private static final String FAKE_CSV = "John\t123-456\nJane\t456-123\n";

    @Test
    public void constructorShouldThrowNPEForNullContent() {
        assertThrows(NullPointerException.class, () -> {
            new EmailAttachment(null, PDF_CONTENT_TYPE, PDF_FILE_NAME);
        });
    }

    @Test
    public void constructorShouldThrowNPEForNullContentType() {
        assertThrows(NullPointerException.class, () -> {
            new EmailAttachment(CONTENT, null, PDF_FILE_NAME);
        });
    }

    @Test
    public void constructorShouldThrowNPEForNullFileName() {
        assertThrows(NullPointerException.class, () -> {
            new EmailAttachment(CONTENT, PDF_CONTENT_TYPE, null);
        });
    }

    @Test
    public void shouldCreateEmailWithCSVAttachment() {
        assertThat(EmailAttachment.csv(FAKE_CSV.getBytes(), CSV_FILE_NAME).getContentType())
            .isEqualTo(CSV_CONTENT_TYPE);
    }
}
