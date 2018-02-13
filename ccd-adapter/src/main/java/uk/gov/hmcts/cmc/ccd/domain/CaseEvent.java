package uk.gov.hmcts.cmc.ccd.domain;

import java.util.Arrays;

public enum CaseEvent {

    SUBMIT_CLAIM("submitClaimEvent"),
    DEFAULT_CCJ_REQUESTED("DefaultCCJRequested"),
    DEFENCE_SUBMITTED("DefenceSubmitted"),
    MORE_TIME_REQUESTED("MoreTimeRequested"),
    OFFER_REJECTED_BY_CLAIMANT("OfferRejectedByClaimant"),
    OFFER_REJECTED_BY_DEFENDANT("OfferRejectedByDefendant"),
    OFFER_ACCEPTED_BY_CLAIMANT("OfferAcceptedByClaimant"),
    OFFER_ACCEPTED_BY_DEFENDANT("OfferAcceptedByDefendant"),
    OFFER_MADE_BY_CLAIMANT("OfferMadeByClaimant"),
    OFFER_MADE_BY_DEFENDANT("OfferMadeByDefendant");

    private String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CaseEvent event(String value){
        return Arrays.stream(CaseEvent.values())
            .filter(v ->v.value.equals(value))
            .findFirst()
            .orElseThrow(RuntimeException::new);
    }
}
