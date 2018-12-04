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

    public String getFullDefenceResponseEmailBody() {
        return readString("/staff/templates/email/fullDefence/body.txt");
    }

    public String getFullDefenceResponseEmailSubject() {
        return readString("/staff/templates/email/fullDefence/subject.txt");
    }

    public String getDefendantAdmissionResponseEmailBody() {
        return readString("/staff/templates/email/defendantAdmission/body.txt");
    }

    public String getDefendantAdmissionResponseEmailSubject() {
        return readString("/staff/templates/email/defendantAdmission/subject.txt");
    }

    public String getCCJRequestSubmittedEmailBody() {
        return readString("/staff/templates/email/ccjRequestSubmitted/body.txt");
    }

    public String getCCJRequestSubmittedEmailSubject() {
        return readString("/staff/templates/email/ccjRequestSubmitted/subject.txt");
    }

    public String getSettlementAgreementAcceptedEmailBody() {
        return readString("/staff/templates/email/settlementAgreement/accepted/body.txt");
    }

    public String getSettlementAgreementAcceptedEmailSubject() {
        return readString("/staff/templates/email/settlementAgreement/accepted/subject.txt");
    }

    public String getSettlementAgreementRejectedEmailBody() {
        return readString("/staff/templates/email/settlementAgreement/rejected/body.txt");
    }

    public String getSettlementAgreementRejectedEmailSubject() {
        return readString("/staff/templates/email/settlementAgreement/rejected/subject.txt");
    }

    public String getBulkPrintEmailBody() {
        return readString("/staff/templates/email/bulkPrintFailure/body.txt");
    }

    public String getBulkPrintEmailSubject() {
        return readString("/staff/templates/email/bulkPrintFailure/subject.txt");
    }

    public String getPaidInFullEmailBody() {
        return readString("/staff/templates/email/paidInFull/body.txt");
    }

    public String getPaidInFullEmailSubject() {
        return readString("/staff/templates/email/paidInFull/subject.txt");
    }


    public String getReDeterminationRequestEmailBody() {
        return readString("/staff/templates/email/redeterminationRequest/body.txt");
    }

    public String getReDeterminationRequestEmailSubject() {
        return readString("/staff/templates/email/redeterminationRequest/subject.txt");
    }
}
