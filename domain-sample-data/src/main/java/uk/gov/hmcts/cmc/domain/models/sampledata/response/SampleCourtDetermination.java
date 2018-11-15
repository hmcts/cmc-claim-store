package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination.CourtDeterminationBuilder;

import static java.math.BigDecimal.TEN;
import static uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType.COURT;

public class SampleCourtDetermination {

    private SampleCourtDetermination() {
        super();
    }

    public static CourtDeterminationBuilder builder() {
        return CourtDetermination.builder();
    }

    public static CourtDetermination immediately() {
        return builder()
            .courtDecision(SamplePaymentIntention.immediately())
            .disposableIncome(TEN)
            .build();
    }

    public static CourtDetermination bySetDate() {
        return builder()
            .courtDecision(SamplePaymentIntention.bySetDate())
            .courtPaymentIntention(SamplePaymentIntention.bySetDate())
            .disposableIncome(TEN)
            .decisionType(COURT)
            .build();
    }

    public static CourtDetermination instalments() {
        return builder()
            .courtDecision(SamplePaymentIntention.instalments())
            .disposableIncome(TEN)
            .decisionType(COURT)
            .build();
    }
}
