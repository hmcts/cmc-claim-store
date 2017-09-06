package uk.gov.hmcts.cmc.email;

import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;

public class EmailAttachmentTest {

    private static final ByteArrayResource CONTENT = new ByteArrayResource(new byte[] { 1, 2, 3, 4 });
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String FILE_NAME = "document.pdf";

    @Test(expected = NullPointerException.class)
    public void constructorShouldThrowNPEForNullContent() throws Exception {
        new EmailAttachment(null, CONTENT_TYPE, FILE_NAME);
    }

    @Test(expected = NullPointerException.class)
    public void constructorShouldThrowNPEForNullContentType() throws Exception {
        new EmailAttachment(CONTENT, null, FILE_NAME);
    }

    @Test(expected = NullPointerException.class)
    public void constructorShouldThrowNPEForNullFileName() {
        new EmailAttachment(CONTENT, CONTENT_TYPE, null);
    }

}
