package uk.gov.hmcts.cmc.domain.models.sampledata.response;

import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination.CourtDeterminationBuilder;

public class SampleCourtDetermination {

    private SampleCourtDetermination() {
        super();
    }

    public static CourtDeterminationBuilder builder() {
        return CourtDetermination.builder();
    }

    public static CourtDetermination immediately() {
        return builder()
            .courtCalculatedPaymentIntention(SamplePaymentIntention.immediately())
            .build();
    }

    public static CourtDetermination bySetDate() {
        return builder()
            .courtCalculatedPaymentIntention(SamplePaymentIntention.bySetDate())
            .build();
    }

    public static CourtDetermination instalments() {
        return builder()
            .courtCalculatedPaymentIntention(SamplePaymentIntention.instalments())
            .build();
    }
}
