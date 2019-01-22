package uk.gov.hmcts.cmc.claimstore.appinsights;

public enum AppInsightsEvent {
    CLAIM_ISSUED_LEGAL("Claim issued - Legal"),
    CLAIM_ISSUED_CITIZEN("Claim issued - Citizen"),
    CLAIM_ATTEMPT_DUPLICATE("Claim attempt - Duplicate"),
    RESPONSE_FULL_DEFENCE_SUBMITTED("Defendant Response - Full defence submitted"),
    RESPONSE_FULL_DEFENCE_SUBMITTED_STATES_PAID("Defendant Response - Full defence submitted - States Paid"),
    RESPONSE_FULL_ADMISSION_SUBMITTED_IMMEDIATELY("Defendant Response - Full admission submitted - Immediate"),
    RESPONSE_FULL_ADMISSION_SUBMITTED_SET_DATE("Defendant Response - Full admission submitted - By set date"),
    RESPONSE_FULL_ADMISSION_SUBMITTED_INSTALMENTS("Defendant Response - Full admission submitted - Instalments"),
    RESPONSE_PART_ADMISSION_SUBMITTED_IMMEDIATELY("Defendant Response - Part admission submitted - Immediate"),
    RESPONSE_PART_ADMISSION_SUBMITTED_SET_DATE("Defendant Response - Part admission submitted - By set date"),
    RESPONSE_PART_ADMISSION_SUBMITTED_INSTALMENTS("Defendant Response - Part admission submitted - Instalments"),
    RESPONSE_PART_ADMISSION_SUBMITTED_STATES_PAID("Defendant Response - Part admission submitted - States Paid"),
    RESPONSE_PART_ADMISSION("Defendant Response - Part admission submitted"),
    RESPONSE_SUBMITTED("Defendant Response Submitted"),
    OFFER_MADE("Offer made"),
    OFFER_REJECTED("Offer rejected"),
    SETTLEMENT_AGREEMENT_REJECTED("Settlement agreement rejected"),
    SETTLEMENT_AGREEMENT_REACHED("Settlement agreement reached"),
    SETTLEMENT_AGREEMENT_REACHED_BY_ADMISSION("Settlement - Settlement agreement reached by admission"),
    SETTLEMENT_REACHED("Settlement reached"),
    CCJ_REQUESTED("CCJ requested"),
    CCJ_REQUESTED_BY_ADMISSION("CCJ - Requested by admission"),
    CCJ_REQUESTED_AFTER_SETTLEMENT_BREACH("CCJ - Settlement agreement breached"),
    RESPONSE_MORE_TIME_REQUESTED("Response - more time requested"),
    RESPONSE_MORE_TIME_REQUESTED_PAPER("Response - more time requested paper"),
    CLAIMANT_RESPONSE_GENERATED_OFFER_MADE("Claimant response - Generated offer made"),
    BULK_PRINT_FAILED("Bulk print failed"),
    SCHEDULER_JOB_FAILED("Scheduler job failed"),
    CLAIMANT_RESPONSE_REJECTED("Claimant response - rejected"),
    CLAIMANT_RESPONSE_ACCEPTED("Claimant response - accepted"),
    CCD_ASYNC_FAILURE("CCD Async handling - failure"),
    REDETERMINATION_REQUESTED("CCJ - Requested by re-determination ");

    private String displayName;

    AppInsightsEvent(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
