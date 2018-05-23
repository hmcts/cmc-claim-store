package uk.gov.hmcts.cmc.domain.models.response;

public enum ResponseType {

    FULL_DEFENCE("reject all of the claim"),
    FULL_ADMISSION("admit all of the claim"),
    PART_ADMISSION("admit part of the claim");

    String description;

    ResponseType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}
