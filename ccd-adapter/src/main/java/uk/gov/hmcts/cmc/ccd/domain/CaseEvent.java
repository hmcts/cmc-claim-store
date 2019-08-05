package uk.gov.hmcts.cmc.ccd.domain;

public enum CaseEvent {
    CREATE_CASE("CreateClaim"),
    ISSUE_CASE("IssueClaim"),
    MORE_TIME_REQUESTED_PAPER("MoreTimeRequestedPaper"),
    TEST_SUPPORT_UPDATE("TestSupportUpdate"),
    LINK_LETTER_HOLDER("LinkLetterHolder"),
    LINK_SEALED_CLAIM("LinkSealedClaimDocument"),
    CLAIM_ISSUE_RECEIPT_UPLOAD("ClaimIssueReceiptUpload"),
    SEALED_CLAIM_UPLOAD("SealedClaimUpload"),
    PIN_GENERATION_OPERATIONS("PinGenerationOperations"),
    SENDING_CLAIMANT_NOTIFICATION("SendingClaimantNotification"),
    SENDING_RPA("SendingRPA"),
    LINK_DEFENDANT("LinkDefendant"),
    MORE_TIME_REQUESTED_ONLINE("MoreTimeRequestedOnline"),
    DEFAULT_CCJ_REQUESTED("DefaultCCJRequested"),
    DISPUTE("DisputesAll"),
    ALREADY_PAID("StatesPaid"),
    FULL_ADMISSION("AdmitAll"),
    PART_ADMISSION("AdmitPart"),
    DEFENDANT_RESPONSE_UPLOAD("DefendantResponseReceiptUpload"),
    DIRECTIONS_QUESTIONNAIRE_DEADLINE("DirectionsQuestionnaireDeadline"),
    CLAIMANT_RESPONSE_ACCEPTATION("ClaimantAccepts"),
    CLAIMANT_RESPONSE_REJECTION("ClaimantRejects"),
    OFFER_MADE_BY_CLAIMANT("OfferMadeByClaimant"),
    OFFER_MADE_BY_DEFENDANT("OfferMadeByDefendant"),
    OFFER_REJECTED_BY_CLAIMANT("OfferRejectedByClaimant"),
    OFFER_REJECTED_BY_DEFENDANT("OfferRejectedByDefendant"),
    OFFER_SIGNED_BY_CLAIMANT("OfferSignedByClaimant"),
    OFFER_COUNTER_SIGNED_BY_DEFENDANT("OfferCounterSignedByDefendant"),
    AGREEMENT_SIGNED_BY_CLAIMANT("AgreementSignedByClaimant"),
    AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT("AgreementCounterSignedByDefendant"),
    AGREEMENT_REJECTED_BY_DEFENDANT("AgreementRejectedByDefendant"),
    SETTLEMENT_AGREEMENT_UPLOAD("SettlementAgreementUpload"),
    CCJ_REQUESTED("CCJRequested"),
    INTERLOCUTORY_JUDGMENT("InterlocutoryJudgment"),
    REJECT_ORGANISATION_PAYMENT_PLAN("RejectOrganisationPaymentPlan"),
    REFER_TO_JUDGE_BY_CLAIMANT("ReferToJudgeByClaimant"),
    REFER_TO_JUDGE_BY_DEFENDANT("ReferToJudgeByDefendant"),
    SETTLED_PRE_JUDGMENT("SettledPreJudgment"),
    SUPPORT_UPDATE("SupportUpdate"),
    GENERATE_ORDER("GenerateOrder"),
    ACTION_REVIEW_COMMENTS("ActionReviewComments"),
    ASSIGNING_FOR_DIRECTIONS("AssigningForDirections"),
    REFERRED_TO_MEDIATION("ReferredToMediation"),
    DRAW_ORDER("DrawOrder"),
    RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS("ResetClaimSubmissionOperationIndicators"),
    ORDER_REVIEW_REQUESTED("OrderReviewRequested");

    private String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
