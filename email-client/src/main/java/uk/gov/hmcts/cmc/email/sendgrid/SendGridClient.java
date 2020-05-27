package uk.gov.hmcts.cmc.email.sendgrid;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.email.EmailData;
import uk.gov.hmcts.cmc.email.EmailSendFailedException;

import java.io.IOException;

@Component
public class SendGridClient {
    private final SendGrid sendGrid;

    @Autowired
    public SendGridClient(
        SendGridFactory factory,
        @Value("${SENDGRID_API_KEY}") String apiKey,
        @Value("false") Boolean testing
    ) {
        sendGrid = factory.createSendGrid(apiKey, testing);
    }

    public void sendEmail(String from, EmailData emailData) throws IOException {
        Email sender = new Email(from);
        String subject = emailData.getSubject();
        Email recipient = new Email(emailData.getTo());
        Content content = new Content(MediaType.TEXT_PLAIN_VALUE, emailData.getMessage());
        Mail mail = new Mail(sender, subject, recipient, content);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        if (response.getStatusCode() / 100 != 2) {
            throw new EmailSendFailedException(new HttpException(String.format(
                "SendGrid returned a non-success response (%d); body: %s",
                response.getStatusCode(),
                response.getBody()
            )));
        }
    }
}
