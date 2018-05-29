package uk.gov.hmcts.cmc.claimstore.appinsights;

public enum AppInsightsEvent {
    CLAIM_ISSUED_LEGAL("Claim issued - Legal"),
    CLAIM_ISSUED_CITIZEN("Claim issued - Citizen"),
    RESPONSE_FULL_DEFENCE_SUBMITTED("Response full defence submitted"),
    RESPONSE_FULL_ADMISSION_SUBMITTED("Response full admission submitted"),
    RESPONSE_PART_ADMISSION_SUBMITTED("Response part admission submitted"),
    OFFER_MADE("Offer made"),
    OFFER_REJECTED("Offer rejected"),
    SETTLEMENT_REACHED("Settlement reached"),
    CCJ_REQUESTED("CCJ requested"),
    RESPONSE_MORE_TIME_REQUESTED("Response - more time requested"),
    RESPONSE_MORE_TIME_REQUESTED_PAPER("Response - more time requested paper"),
    BULK_PRINT_FAILED("Bulk print failed");

    private String displayName;

    AppInsightsEvent(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
