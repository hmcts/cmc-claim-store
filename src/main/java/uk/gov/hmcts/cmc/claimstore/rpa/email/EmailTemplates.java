package uk.gov.hmcts.cmc.claimstore.rpa.email;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Component("rpa/email-templates")
public class EmailTemplates {
    public String getClaimIssuedEmailSubject() {
        return readString("/rpa/templates/email/claimIssued/subject.txt");
    }

    public String getClaimIssuedEmailBody() {
        return readString("/rpa/templates/email/claimIssued/body.txt");
    }

    private String readString(String resourcePath) {
        return new String(
            readBytes(resourcePath),
            Charset.forName("UTF-8")
        );
    }

    private byte[] readBytes(String resourcePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
