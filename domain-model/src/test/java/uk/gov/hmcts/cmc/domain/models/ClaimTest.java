package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.RESPONSE_DEADLINE;

public class ClaimTest {

    @Test
    public void isEqualWhenTheSameObjectWithDefaultValuesIsGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isEqualTo(claim);
    }

    @Test
    public void isEqualWhenTheSameObjectWithCustomValuesIsGiven() {
        Claim claim = customValues();

        assertThat(claim).isEqualTo(claim);
    }

    @Test
    public void isNotEqualWhenNullGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isNotEqualTo(null);
    }

    @Test
    public void isNotEqualWhenDifferentTypeObjectGiven() {
        Claim claim = SampleClaim.getDefault();
        assertThat(claim).isNotEqualTo(new HashMap<>());
    }

    @Test
    public void isNotEqualWhenDifferentClaimObjectGiven() {
        Claim claim1 = customValues();
        Claim claim2 = SampleClaim.getDefault();
        assertThat(claim1).isNotEqualTo(claim2);
    }

    @Test
    public void isNotEqualWhenOnlyOneFieldIsDifferent() {
        Claim claim1 = customCreatedAt(NOW_IN_LOCAL_ZONE.plusNanos(1));
        Claim claim2 = customCreatedAt(NOW_IN_LOCAL_ZONE);
        assertThat(claim1).isNotEqualTo(claim2);
    }

    @Test
    public void copyCreatesNewInstanceOfClaim() {
        Claim claim1 = SampleClaim.getDefault();
        Claim claim2 = claim1.copy(BigDecimal.ONE, BigDecimal.ZERO);

        assertThat(claim1).isNotEqualTo(claim2);
        assertThat(claim1.getTotalAmountTillDateOfIssue()).isNotEqualTo(claim2.getTotalAmountTillDateOfIssue());
        assertThat(claim1.getTotalAmountTillToday()).isNotEqualTo(claim2.getTotalAmountTillToday());

        assertThat(claim1.getId()).isEqualTo(claim2.getId());
        assertThat(claim1.getClaimData()).isEqualTo(claim2.getClaimData());
        assertThat(claim1.getCountyCourtJudgment()).isEqualTo(claim2.getCountyCourtJudgment());
        assertThat(claim1.getCountyCourtJudgmentRequestedAt()).isEqualTo(claim2.getCountyCourtJudgmentRequestedAt());
        assertThat(claim1.getCreatedAt()).isEqualTo(claim2.getCreatedAt());
        assertThat(claim1.getDefendantEmail()).isEqualTo(claim2.getDefendantEmail());
        assertThat(claim1.getDefendantId()).isEqualTo(claim2.getDefendantId());
        assertThat(claim1.getExternalId()).isEqualTo(claim2.getExternalId());
        assertThat(claim1.getIssuedOn()).isEqualTo(claim2.getIssuedOn());
        assertThat(claim1.getLetterHolderId()).isEqualTo(claim2.getLetterHolderId());
        assertThat(claim1.getReferenceNumber()).isEqualTo(claim2.getReferenceNumber());
        assertThat(claim1.getRespondedAt()).isEqualTo(claim2.getRespondedAt());
        assertThat(claim1.getResponseDeadline()).isEqualTo(claim2.getResponseDeadline());
        assertThat(claim1.getResponse()).isEqualTo(claim2.getResponse());
        assertThat(claim1.getSettlementReachedAt()).isEqualTo(claim2.getSettlementReachedAt());
        assertThat(claim1.getSettlement()).isEqualTo(claim2.getSettlement());
        assertThat(claim1.getSubmitterEmail()).isEqualTo(claim2.getSubmitterEmail());
        assertThat(claim1.getSubmitterId()).isEqualTo(claim2.getSubmitterId());
    }

    private static Claim customValues() {
        return customCreatedAt(NOW_IN_LOCAL_ZONE);
    }

    private static Claim customCreatedAt(LocalDateTime createdAt) {
        return SampleClaim.builder()
            .withClaimId(1L)
            .withSubmitterId("3")
            .withLetterHolderId("3")
            .withDefendantId("4")
            .withExternalId("external-id")
            .withReferenceNumber("ref number")
            .withCreatedAt(createdAt)
            .withIssuedOn(ISSUE_DATE)
            .withResponseDeadline(RESPONSE_DEADLINE)
            .withMoreTimeRequested(false)
            .withSubmitterEmail("claimant@mail.com")
            .build();
    }
}
