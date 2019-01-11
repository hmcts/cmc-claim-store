package uk.gov.hmcts.cmc.claimstore.appinsights;

public enum AppInsightsEvent {
    CLAIM_ISSUED_LEGAL("Claim issued - Legal"),
    CLAIM_ISSUED_CITIZEN("Claim issued - Citizen"),
    RESPONSE_FULL_DEFENCE_SUBMITTED("Defendant Response - Full defence submitted"),
    RESPONSE_FULL_ADMISSION_SUBMITTED_IMMEDIATELY("Response - Full admission submitted - Payment: Immediate"),
    RESPONSE_FULL_ADMISSION_SUBMITTED_SET_DATE("Response - Full admission submitted - Payment: By set date"),
    RESPONSE_FULL_ADMISSION_SUBMITTED_INSTALMENTS("Response - Full admission submitted - Payment: Instalments"),
    RESPONSE_PART_ADMISSION_SUBMITTED_IMMEDIATELY("Response - Part admission submitted - Payment: Immediate"),
    RESPONSE_PART_ADMISSION_SUBMITTED_SET_DATE("Response - Part admission submitted - Payment: By set date"),
    RESPONSE_PART_ADMISSION_SUBMITTED_INSTALMENTS("Response - Part admission submitted - Payment: Instalments"),
    OFFER_MADE("Offer made"),
    OFFER_REJECTED("Offer rejected"),
    SETTLEMENT_AGREEMENT_REJECTED("Settlement agreement rejected"),
    SETTLEMENT_AGREEMENT_REACHED("Settlement agreement reached"),
    SETTLEMENT_REACHED("Settlement reached"),
    CCJ_REQUESTED("CCJ requested"),
    RESPONSE_MORE_TIME_REQUESTED("Defendant Response - More time requested"),
    RESPONSE_MORE_TIME_REQUESTED_PAPER("Defendant Response - More time requested paper"),
    CLAIMANT_RESPONSE_GENERATED_OFFER_MADE("Claimant Response - Generated offer made"),
    BULK_PRINT_FAILED("Bulk print failed"),
    SCHEDULER_JOB_FAILED("Scheduler job failed"),
    CLAIMANT_RESPONSE_REJECTED("Claimant Response - rejected"),
    CLAIMANT_RESPONSE_ACCEPTED("Claimant Response - accepted"),
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
