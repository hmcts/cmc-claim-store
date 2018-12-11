package uk.gov.hmcts.cmc.ccd.deprecated.domain;

public enum CaseEvent {

    SUBMIT_PRE_PAYMENT("SubmitPrePayment"),
    SUBMIT_POST_PAYMENT("SubmitPostPayment"),
    DEFAULT_CCJ_REQUESTED("DefaultCCJRequested"),
    CCJ_BY_ADMISSION("CCJByAdmission"),
    DISPUTE("DisputesAll"),
    ALREADY_PAID("StatesPaid"),
    FULL_ADMISSION("AdmitAll"),
    PART_ADMISSION("AdmitPart"),
    DIRECTIONS_QUESTIONNAIRE_DEADLINE("DirectionsQuestionnaireDeadline"),
    CLAIMANT_RESPONSE_ACCEPTATION("ClaimantResponseAcceptation"),
    CLAIMANT_RESPONSE_REJECTION("ClaimantResponseRejection"),
    MORE_TIME_REQUESTED_ONLINE("MoreTimeRequestedOnline"),
    MORE_TIME_REQUESTED_PAPER("MoreTimeRequestedPaper"),
    TEST_SUPPORT_UPDATE("TestSupportUpdate"),
    OFFER_REJECTED_BY_CLAIMANT("OfferRejectedByClaimant"),
    OFFER_REJECTED_BY_DEFENDANT("OfferRejectedByDefendant"),
    OFFER_ACCEPTED_BY_CLAIMANT("OfferAcceptedByClaimant"),
    OFFER_ACCEPTED_BY_DEFENDANT("OfferAcceptedByDefendant"),
    OFFER_MADE_BY_CLAIMANT("OfferMadeByClaimant"),
    OFFER_MADE_BY_DEFENDANT("OfferMadeByDefendant"),
    SETTLED_PRE_JUDGMENT("SettledPreJudgment"),
    LINK_DEFENDANT("LinkDefendant"),
    LINK_SEALED_CLAIM("LinkSealedClaimDocument");

    private String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
