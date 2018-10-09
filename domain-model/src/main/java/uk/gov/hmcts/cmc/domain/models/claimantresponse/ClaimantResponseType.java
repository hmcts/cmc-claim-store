package uk.gov.hmcts.cmc.domain.models.claimantresponse;

public enum ClaimantResponseType {
    ACCEPTATION("acceptation"),
    REJECTION("rejection");

    String description;

    ClaimantResponseType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
