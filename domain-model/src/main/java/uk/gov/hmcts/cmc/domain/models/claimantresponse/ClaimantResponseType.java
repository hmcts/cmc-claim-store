package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import lombok.Getter;

@Getter
public enum ClaimantResponseType {
    ACCEPTATION("acceptation"),
    REJECTION("rejection");

    private final String description;

    ClaimantResponseType(String description) {
        this.description = description;
    }
}
