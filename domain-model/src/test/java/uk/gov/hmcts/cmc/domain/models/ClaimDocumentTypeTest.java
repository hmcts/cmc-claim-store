package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CCJ_REQUEST;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.CLAIM_ISSUE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_PIN_LETTER;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.DEFENDANT_RESPONSE_RECEIPT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_DIRECTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.ORDER_SANCTIONS;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.SETTLEMENT_AGREEMENT;
import static uk.gov.hmcts.cmc.domain.models.ClaimDocumentType.fromValue;

public class ClaimDocumentTypeTest {

    @Test
    public void shouldCreateEnumFromValidString() {
        assertThat(fromValue("sealedClaim"))
            .isEqualTo(SEALED_CLAIM);
        assertThat(fromValue("legalSealedClaim"))
            .isEqualTo(SEALED_CLAIM);
        assertThat(fromValue("orderDirections"))
            .isEqualTo(ORDER_DIRECTIONS);
        assertThat(fromValue("orderSanctions"))
            .isEqualTo(ORDER_SANCTIONS);
        assertThat(fromValue("claimIssueReceipt"))
            .isEqualTo(CLAIM_ISSUE_RECEIPT);
        assertThat(fromValue("defendantResponseReceipt"))
            .isEqualTo(DEFENDANT_RESPONSE_RECEIPT);
        assertThat(fromValue("settlementAgreement"))
            .isEqualTo(SETTLEMENT_AGREEMENT);
    }

    @Test
    public void shouldCreateEnumFromEnumName() {
        assertThat(fromValue("SEALED_CLAIM"))
            .isEqualTo(SEALED_CLAIM);
        assertThat(fromValue("ORDER_DIRECTIONS"))
            .isEqualTo(ORDER_DIRECTIONS);
        assertThat(fromValue("ORDER_SANCTIONS"))
            .isEqualTo(ORDER_SANCTIONS);
        assertThat(fromValue("CLAIM_ISSUE_RECEIPT"))
            .isEqualTo(CLAIM_ISSUE_RECEIPT);
        assertThat(fromValue("DEFENDANT_RESPONSE_RECEIPT"))
            .isEqualTo(DEFENDANT_RESPONSE_RECEIPT);
        assertThat(fromValue("SETTLEMENT_AGREEMENT"))
            .isEqualTo(SETTLEMENT_AGREEMENT);
        assertThat(fromValue("CCJ_REQUEST"))
            .isEqualTo(CCJ_REQUEST);
        assertThat(fromValue("DEFENDANT_PIN_LETTER"))
            .isEqualTo(DEFENDANT_PIN_LETTER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfUnknownEnum() {
        fromValue("nope");
    }
}
