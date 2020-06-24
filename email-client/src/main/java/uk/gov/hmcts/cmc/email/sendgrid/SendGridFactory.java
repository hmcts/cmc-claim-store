package uk.gov.hmcts.cmc.email.sendgrid;

import com.sendgrid.SendGrid;
import org.springframework.stereotype.Component;

@Component
public class SendGridFactory {
    public SendGrid createSendGrid(String apiKey, boolean testing) {
        return new SendGrid(apiKey, testing);
    }
}
