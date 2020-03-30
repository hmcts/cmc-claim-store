package uk.gov.hmcts.cmc.claimstore.config.properties.pdf;

import org.springframework.stereotype.Component;

import static uk.gov.hmcts.cmc.claimstore.utils.ResourceReader.readBytes;

@Component
public class DocumentTemplates {

    public byte[] getDefendantResponseReceipt() {
        return readBytes("/citizen/templates/document/defendantResponseReceipt.html");
    }

    public byte[] getClaimantResponseReceipt() {
        return readBytes("/citizen/templates/document/claimantResponseReceipt.html");
    }

    public byte[] getClaimIssueReceipt() {
        return readBytes("/citizen/templates/document/claimIssueReceipt.html");
    }

    public byte[] getReviewOrder() {
        return readBytes("/citizen/templates/document/reviewOrder.html");
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

    public byte[] getCountyCourtJudgmentByRequest() {
        return readBytes("/staff/templates/document/countyCourtJudgmentDetails.html");
    }

    public byte[] getSettlementAgreement() {
        return readBytes("/staff/templates/document/settlementAgreement.html");
    }

    public byte[] getLegalOrderCoverSheet() {
        return readBytes("/staff/templates/document/legalOrderCoverSheet.html");
    }

    public byte[] getClaimantDirectionsQuestionnaire() {
        return readBytes("/citizen/templates/document/claimantDirectionsQuestionnaire.html");
    }
}
