package uk.gov.hmcts.cmc.claimstore.appinsights;

public enum AppInsightsEvent {
    CLAIM_ISSUED_LEGAL("Claim issued - Legal"),
    CLAIM_ISSUED_CITIZEN("Claim issued - Citizen"),
    RESPONSE_FULL_DEFENCE_SUBMITTED("Response full defence submitted"),
    RESPONSE_FULL_ADMISSION_SUBMITTED("Response full admission submitted"),
    RESPONSE_PART_ADMISSION_SUBMITTED("Response part admission submitted"),
    OFFER_MADE("Offer made"),
    OFFER_REJECTED("Offer rejected"),
    SETTLEMENT_AGREEMENT_REJECTED("Settlement agreement rejected"),
    SETTLEMENT_AGREEMENT_REACHED("Settlement agreement reached"),
    SETTLEMENT_AGREEMENT_REACHED_BY_ADMISSION("Settlement - Settlement agreement reached by admission"),
    SETTLEMENT_REACHED("Settlement reached"),
    CCJ_REQUESTED("CCJ requested"),
    CCJ_REQUESTED_BY_ADMISSION("CCJ - CCJ by admission requested"),
    RESPONSE_MORE_TIME_REQUESTED("Response - more time requested"),
    RESPONSE_MORE_TIME_REQUESTED_PAPER("Response - more time requested paper"),
    CLAIMANT_RESPONSE_GENERATED_OFFER_MADE("Claimant response - Generated offer made"),
    BULK_PRINT_FAILED("Bulk print failed"),
    SCHEDULER_JOB_FAILED("Scheduler job failed"),
    CLAIMANT_RESPONSE_REJECTED("Claimant response - rejected"),
    CLAIMANT_RESPONSE_ACCEPTED("Claimant response - accepted"),
    CCD_ASYNC_FAILURE("CCD Async handling - failure");

    private String displayName;

    AppInsightsEvent(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
