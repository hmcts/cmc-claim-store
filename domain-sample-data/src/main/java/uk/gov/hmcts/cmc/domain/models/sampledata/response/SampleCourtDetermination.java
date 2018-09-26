package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination.CourtDeterminationBuilder;

import static java.math.BigDecimal.TEN;

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
            .disposableIncome(TEN)
            .build();
    }

    public static CourtDetermination instalments() {
        return builder()
            .courtDecision(SamplePaymentIntention.instalments())
            .disposableIncome(TEN)
            .build();
    }
}
