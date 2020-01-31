package uk.gov.hmcts.cmc.ccd.domain.evidence;

public enum CCDEvidenceType {
    CONTRACTS_AND_AGREEMENTS("Contracts and agreements"),
    EXPERT_WITNESS("Expert witness"),
    CORRESPONDENCE("Letters, emails and other correspondence"),
    PHOTO("Photo evidence"),
    RECEIPTS("Receipts"),
    STATEMENT_OF_ACCOUNT("Statements of account"),
    OTHER("Other");

    private final String description;

    CCDEvidenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
