package uk.gov.hmcts.cmc.domain.models;

import java.util.Arrays;
import java.util.List;

public enum ClaimDocumentType {
    ORDER_DIRECTIONS("orderDirections"),
    ORDER_SANCTIONS("orderSanctions"),
    SEALED_CLAIM("legalSealedClaim", "sealedClaim"),
    CLAIM_ISSUE_RECEIPT("claimIssueReceipt"),
    DEFENDANT_RESPONSE_RECEIPT("defendantResponseReceipt"),
    CLAIMANT_RESPONSE_RECEIPT,
    CCJ_REQUEST,
    SETTLEMENT_AGREEMENT("settlementAgreement"),
    DEFENDANT_PIN_LETTER,
    CLAIMANT_DIRECTIONS_QUESTIONNAIRE("claimantHearingRequirement"),
    REVIEW_ORDER,
    MEDIATION_AGREEMENT,
    COVER_SHEET,
    PAPER_RESPONSE_FULL_ADMIT,
    PAPER_RESPONSE_PART_ADMIT,
    PAPER_RESPONSE_STATES_PAID,
    PAPER_RESPONSE_MORE_TIME,
    PAPER_RESPONSE_DISPUTES_ALL;

    private final List<String> values;

    ClaimDocumentType(String... values) {
        this.values = Arrays.asList(values);
    }

    public List<String> getValues() {
        return values;
    }

    public static ClaimDocumentType fromValue(String value) {
        return Arrays.stream(values())
            .filter(v -> v.values.contains(value) || v.name().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown Claim Document Type: " + value));
    }
}
