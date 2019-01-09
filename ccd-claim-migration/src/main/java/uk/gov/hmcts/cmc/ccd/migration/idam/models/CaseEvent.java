package uk.gov.hmcts.cmc.ccd.migration.idam.models;

public enum CaseEvent {

    SUBMIT_PRE_PAYMENT("SubmitPrePayment"),
    SUBMIT_POST_PAYMENT("SubmitPostPayment"),
    LINK_DEFENDANT("LinkDefendant"),
    MORE_TIME_REQUESTED_ONLINE("MoreTimeRequestedOnline"),
    DEFAULT_CCJ_REQUESTED("DefaultCCJRequested"),
    DISPUTE("DisputesAll"),
    ALREADY_PAID("StatesPaid"),
    FULL_ADMISSION("AdmitAll"),
    PART_ADMISSION("AdmitPart"),
    CCJ_BY_ADMISSION("CCJByAdmission"),
    CCJ_BY_DETERMINATION("CCJByDetermination"),
    DIRECTIONS_QUESTIONNAIRE_DEADLINE("DirectionsQuestionnaireDeadline"),
    CLAIMANT_RESPONSE_ACCEPTATION("ClaimantResponseAcceptation"),
    CLAIMANT_RESPONSE_REJECTION("ClaimantResponseRejection"),
    OFFER_REJECTED_BY_CLAIMANT("OfferRejectedByClaimant"),
    OFFER_REJECTED_BY_DEFENDANT("OfferRejectedByDefendant"),
    OFFER_ACCEPTED_BY_CLAIMANT("OfferAcceptedByClaimant"),
    OFFER_ACCEPTED_BY_DEFENDANT("OfferAcceptedByDefendant"),
    OFFER_MADE_BY_CLAIMANT("OfferMadeByClaimant"),
    OFFER_MADE_BY_DEFENDANT("OfferMadeByDefendant"),
    SETTLED_PRE_JUDGMENT("SettledPreJudgment");

    private String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
