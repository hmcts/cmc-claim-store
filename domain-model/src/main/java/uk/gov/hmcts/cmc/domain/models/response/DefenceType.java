package uk.gov.hmcts.cmc.domain.models.response;

public enum DefenceType {
    DISPUTE("I dispute all the claim"),
    ALREADY_PAID("I have paid what I believe I owe"),
    PART_ADMISSION("I reject part of the claim"),
    FULL_ADMISSION("I admit all of the claim");

    String description;

    DefenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
