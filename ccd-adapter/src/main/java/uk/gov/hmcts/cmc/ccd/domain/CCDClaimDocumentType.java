package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDClaimDocumentType {

    CLAIM_ISSUE_RECEIPT("ClaimReceiptUpload"),
    SEALED_CLAIM("SealedClaimUpload"),
    DEFENDANT_RESPONSE_RECEIPT("DefendantResponseUpload"),
    CCJ_REQUEST("CcjRequestUpload"),
    SETTLEMENT_AGREEMENT("SettlementAgreementUpload"),
    DEFENDANT_PIN_LETTER("DefendantPinLetterUpload");

    private String typeName;

    CCDClaimDocumentType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
