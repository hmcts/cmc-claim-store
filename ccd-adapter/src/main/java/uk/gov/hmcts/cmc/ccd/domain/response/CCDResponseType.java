package uk.gov.hmcts.cmc.ccd.domain.response;

public enum CCDResponseType {
    FULL_DEFENCE("reject all of the claim"),
    FULL_ADMISSION("admit all of the claim"),
    PART_ADMISSION("admit part of the claim");

    String description;

    CCDResponseType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}
