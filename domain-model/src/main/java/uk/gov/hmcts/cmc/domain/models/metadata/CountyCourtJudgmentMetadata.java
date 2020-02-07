package uk.gov.hmcts.cmc.domain.models.metadata;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CountyCourtJudgmentMetadata {
    private final LocalDateTime requestedAt;
    private final CountyCourtJudgmentType type;
    private final PaymentPlanMetadata repaymentPlan;

    static CountyCourtJudgmentMetadata fromClaim(Claim claim) {
        CountyCourtJudgment ccj = claim.getCountyCourtJudgment();
        if (ccj == null) {
            return null;
        }

        return new CountyCourtJudgmentMetadata(
            claim.getCountyCourtJudgmentRequestedAt(),
            ccj.getCcjType(),
            PaymentPlanMetadata.fromCountyCourtJudgment(ccj)
        );
    }
}
