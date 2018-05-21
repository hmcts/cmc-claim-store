package uk.gov.hmcts.cmc.claimstore.config.properties.emails;

import org.springframework.stereotype.Component;

import static uk.gov.hmcts.cmc.claimstore.utils.ResourceReader.readString;

@Component
public class StaffEmailTemplates {

    public String getClaimIssuedEmailBody() {
        return readString("/staff/templates/email/claimIssued/body.txt");
    }

    public String getClaimIssuedEmailSubject() {
        return readString("/staff/templates/email/claimIssued/subject.txt");
    }

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

    public String getSettlementAgreementEmailBody() {
        return readString("/staff/templates/email/settlementAgreement/body.txt");
    }

    public String getSettlementAgreementEmailSubject() {
        return readString("/staff/templates/email/settlementAgreement/subject.txt");
    }

    public String getBulkPrintEmailBody() {
        return readString("/staff/templates/email/bulkPrintFailure/body.txt");
    }

    public String getBulkPrintEmailSubject() {
        return readString("/staff/templates/email/bulkPrintFailure/subject.txt");
    }
}
