package uk.gov.hmcts.cmc.ccd.domain;

public enum CaseEvent {

    SUBMIT_PRE_PAYMENT("SubmitPrePayment"),
    SUBMIT_POST_PAYMENT("SubmitPostPayment"),
    DEFAULT_CCJ_REQUESTED("DefaultCCJRequested"),
    DISPUTE("DisputesAll"),
    ALREADY_PAID("StatesPaid"),
    FULL_ADMISSION("AdmitAll"),
    PART_ADMISSION("AdmitPart"),
    DIRECTIONS_QUESTIONNAIRE_DEADLINE("DirectionsQuestionnaireDeadline"),
    CLAIMANT_RESPONSE_ACCEPTATION("ClaimantAccepts"),
    CLAIMANT_RESPONSE_REJECTION("ClaimantRejects"),
    CCJ_REQUESTED("CCJRequested"),
    INTERLOCATORY_JUDGEMENT("InterlocatoryJudgement"),
    REJECT_ORGANISATION_PAYMENT_PLAN("RejectOrganisationPaymentPlan"),
    AGREEMENT_SIGNED_BY_CLAIMANT("AgreementSignedByClaimant"),
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
    LINK_SEALED_CLAIM("LinkSealedClaimDocument"),
    REFER_TO_JUDGE_BY_CLAIMANT("ReferToJudgeByClaimant"),
    REFER_TO_JUDGE_BY_DEFENDANT("ReferToJudgeByDefendant");

    private String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
