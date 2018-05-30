package uk.gov.hmcts.cmc.claimstore.rpa.email;

import org.springframework.stereotype.Component;

import static uk.gov.hmcts.cmc.claimstore.utils.ResourceReader.readString;

@Component("rpa/email-templates")
public class EmailTemplates {

    public String getClaimIssuedEmailSubject() {
        return readString("/rpa/templates/email/claimIssued/subject.txt");
    }

    public String getClaimIssuedEmailBody() {
        return readString("/rpa/templates/email/claimIssued/body.txt");
    }
}
