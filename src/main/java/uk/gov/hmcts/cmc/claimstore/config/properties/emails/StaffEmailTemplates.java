package uk.gov.hmcts.cmc.claimstore.config.properties.emails;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Component
public class StaffEmailTemplates {

    public String getDefendantResponseEmailBody() {
        return readString("/staff/templates/email/defendantResponse/body.txt");
    }

    public String getDefendantResponseEmailSubject() {
        return readString("/staff/templates/email/defendantResponse/subject.txt");
    }

    public String getCCJRequestSubmittedEmailBody() {
        return readString("/staff/templates/email/ccjRequestSubmitted/body.txt");
    }

    public String getCCJRequestSubmittedEmailSubject() {
        return readString("/staff/templates/email/ccjRequestSubmitted/subject.txt");
    }

    private String readString(final String resourcePath) {
        return new String(
            readBytes(resourcePath),
            Charset.forName("UTF-8")
        );
    }

    private byte[] readBytes(final String resourcePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
