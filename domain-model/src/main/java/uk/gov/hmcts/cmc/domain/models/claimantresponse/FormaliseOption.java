package uk.gov.hmcts.cmc.domain.models.claimantresponse;

import lombok.Getter;

@Getter
public enum FormaliseOption {
    CCJ("County Court Judgment"),
    SETTLEMENT("Settlement"),
    REFER_TO_JUDGE("Refer to Judge");

    private final String description;

    FormaliseOption(String description) {
        this.description = description;
    }
}
