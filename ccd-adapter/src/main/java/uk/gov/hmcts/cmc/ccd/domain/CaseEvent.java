package uk.gov.hmcts.cmc.ccd.domain;

import java.util.Arrays;

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
    AGREEMENT_SIGNED_BY_BOTH("AgreementSignedByBoth"),
    AGREEMENT_REJECTED_BY_DEFENDANT("AgreementRejectedByDefendant"),
    SETTLEMENT_AGREEMENT_UPLOAD("SettlementAgreementUpload"),
    REVIEW_ORDER_UPLOAD("ReviewOrderUpload"),
    CCJ_REQUESTED("CCJRequested"),
    INTERLOCUTORY_JUDGMENT("InterlocutoryJudgment"),
    REJECT_ORGANISATION_PAYMENT_PLAN("RejectOrganisationPaymentPlan"),
    REFER_TO_JUDGE_BY_CLAIMANT("ReferToJudgeByClaimant"),
    REFER_TO_JUDGE_BY_DEFENDANT("ReferToJudgeByDefendant"),
    SETTLED_PRE_JUDGMENT("SettledPreJudgment"),
    SUPPORT_UPDATE("SupportUpdate"),
    GENERATE_ORDER("GenerateOrder"),
    JUDGE_REVIEW_ORDER("ReviewOrder"),
    COMPLEX_CASE("ComplexCase"),
    REVIEW_COMPLEX_CASE("ReviewComplexCase"),
    ACTION_REVIEW_COMMENTS("ActionReviewComments"),
    ASSIGNING_FOR_LEGAL_ADVISOR_DIRECTIONS("AssigningForDirections"),
    ASSIGNING_FOR_JUDGE_DIRECTIONS("AssigningForJudgeDirections"),
    REFERRED_TO_MEDIATION("ReferredToMediation"),
    DRAW_ORDER("DrawOrder"),
    DRAW_JUDGES_ORDER("DrawJudgesOrder"),
    RESET_CLAIM_SUBMISSION_OPERATION_INDICATORS("ResetClaimSubmissionOperationIndicators"),
    CLAIMANT_DIRECTIONS_QUESTIONNAIRE_UPLOAD("ClaimantDirectionsQuestionnaireUpload"),
    ORDER_REVIEW_REQUESTED("OrderReviewRequested"),
    WAITING_TRANSFER("WaitingTransfer"),
    TRANSFER("Transfer"),
    AUTOMATED_TRANSFER("AutomatedTransfer"),
    LIFT_STAY("LiftStay"),
    REFER_TO_JUDGE("ReferToJudge"),
    //inversion of control
    INITIATE_CLAIM_PAYMENT_CITIZEN("InitiateClaimPaymentCitizen"),
    RESUME_CLAIM_PAYMENT_CITIZEN("ResumeClaimPaymentCitizen"),
    CREATE_LEGAL_REP_CLAIM("CreateLegalRepClaim"),
    STAY_CLAIM("StayClaim"),
    CREATE_CITIZEN_CLAIM("CreateCitizenClaim"),
    REVIEWED_PAPER_RESPONSE("ReviewedPaperResponse"),
    MEDIATION_FAILED("FailedMediation"),
    MEDIATION_SUCCESSFUL("SuccessfulMediation"),
    RESEND_RPA("ResendRpa"),
    PROCEEDS_IN_CASEMAN("CaseProceedsInCaseman"),
    PAPER_RESPONSE_FULL_DEFENCE("PaperResponseFullDefence"),
    ATTACH_SCANNED_DOCS("attachScannedDocs"),
    PAPER_RESPONSE_OCON_9X_FORM("PaperResponseOCON9xForm"),
    UPDATE_CLAIM("UpdateClaim"),
    RESET_PIN("ResetPin"),
    PAPER_RESPONSE_ADMISSION("PaperResponseAdmission"),
    ISSUE_GENERAL_LETTER("IssueGeneralLetter"),
    RESPONSE_MORE_TIME("ResponseMoreTime"),
    PROVIDE_DIRECTIONS("ProvideDirections"),
    MANAGE_DOCUMENTS("ManageDocuments"),
    CLAIMANT_RESPONSE_RECEIPT_UPLOAD("ClaimantResponseReceiptUpload"),
    REVIEW_JUDGE_COMMENTS("ReviewJudgeComments"),
    CCJ_REQUEST_UPLOAD("CCJRequestUpload"),
    CHANGE_CONTACT_DETAILS("ChangeContactDetails");

    private final String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CaseEvent fromValue(String value) {
        return Arrays.stream(values())
            .filter(event -> event.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown event name: " + value));
    }
}
