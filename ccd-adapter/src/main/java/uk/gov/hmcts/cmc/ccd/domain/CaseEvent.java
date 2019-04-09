package uk.gov.hmcts.cmc.ccd.domain;

import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;

import java.util.Arrays;

public enum CaseEvent {

    CREATE_NEW_CASE("IssueClaim"),
    MORE_TIME_REQUESTED_PAPER("MoreTimeRequestedPaper"),
    TEST_SUPPORT_UPDATE("TestSupportUpdate"),
    LINK_SEALED_CLAIM("LinkSealedClaimDocument"),
    LINK_LETTER_HOLDER("LinkLetterHolder"),
    LINK_DEFENDANT("LinkDefendant"),
    MORE_TIME_REQUESTED_ONLINE("MoreTimeRequestedOnline"),
    DEFAULT_CCJ_REQUESTED("DefaultCCJRequested"),
    DISPUTE("DisputesAll"),
    ALREADY_PAID("StatesPaid"),
    FULL_ADMISSION("AdmitAll"),
    PART_ADMISSION("AdmitPart"),
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
    SETTLED_PRE_JUDGMENT("SettledPreJudgment"),
    CCJ_REQUESTED("CCJRequested"),
    INTERLOCUTORY_JUDGMENT("InterlocutoryJudgment"),
    REJECT_ORGANISATION_PAYMENT_PLAN("RejectOrganisationPaymentPlan"),
    REFER_TO_JUDGE_BY_CLAIMANT("ReferToJudgeByClaimant"),
    REFER_TO_JUDGE_BY_DEFENDANT("ReferToJudgeByDefendant"),
    GENERATE_ORDER("GenerateOrder");

    private String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CaseEvent fromValue(String value) {
        return Arrays.stream(values()).filter(event -> event.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Unknown Case Event: " + value));
    }
}
