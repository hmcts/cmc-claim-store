package uk.gov.hmcts.cmc.claimstore.config.properties.pdf;

import org.springframework.stereotype.Component;

import static uk.gov.hmcts.cmc.claimstore.utils.ResourceReader.readBytes;

@Component
public class DocumentTemplates {

    public byte[] getDefendantResponseReceipt() {
        return readBytes("/citizen/templates/document/defendantResponseReceipt.html");
    }

    public byte[] getClaimIssueReceipt() {
        return readBytes("/citizen/templates/document/claimIssueReceipt.html");
    }

    public byte[] getSealedClaim() {
        return readBytes("/staff/templates/document/sealedClaim.html");
    }

    public byte[] getDefendantPinLetter() {
        return readBytes("/staff/templates/document/defendantPinLetter.html");
    }

    public byte[] getLegalSealedClaim() {
        return readBytes("/staff/templates/document/legalSealedClaim.html");
    }

    public byte[] getCountyCourtJudgmentDetails() {
        return readBytes("/staff/templates/document/countyCourtJudgmentDetails.html");
    }

    public byte[] getSettlementAgreement() {
        return readBytes("/staff/templates/document/settlementAgreement.html");
    }

}
