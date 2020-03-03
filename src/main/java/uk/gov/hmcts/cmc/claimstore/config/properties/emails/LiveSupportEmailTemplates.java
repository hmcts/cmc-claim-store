package uk.gov.hmcts.cmc.claimstore.config.properties.emails;

import org.springframework.stereotype.Component;

import static uk.gov.hmcts.cmc.claimstore.utils.ResourceReader.readString;

@Component
public class LiveSupportEmailTemplates {
    public String getBulkPrintEmailBody() {
        return readString("/liveSupport/templates/email/bulkPrintFailure/body.txt");
    }

    public String getBulkPrintEmailSubject() {
        return readString("/liveSupport/templates/email/bulkPrintFailure/subject.txt");
    }
}
