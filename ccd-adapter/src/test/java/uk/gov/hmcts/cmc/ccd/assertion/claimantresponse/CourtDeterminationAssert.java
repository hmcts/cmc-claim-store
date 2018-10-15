package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class CourtDeterminationAssert extends AbstractAssert<CourtDeterminationAssert, CourtDetermination> {
    public CourtDeterminationAssert(CourtDetermination courtDetermination) {
        super(courtDetermination, CourtDeterminationAssert.class);
    }

    public CourtDeterminationAssert isEqualTo(CCDCourtDetermination ccdCourtDetermination) {
        isNotNull();

        actual.getRejectionReason().ifPresent(rejectionReason -> {
            if (!Objects.equals(rejectionReason, ccdCourtDetermination.getRejectionReason())) {
                failWithMessage("Expected CourtDetermination.getRejectionReason to be <%s> but was <%s>",
                    ccdCourtDetermination.getRejectionReason(), rejectionReason);
            }

        });

        assertThat(actual.getCourtPaymentIntention()).isEqualTo(ccdCourtDetermination.getCourtPaymentIntention());
        assertThat(actual.getCourtDecision()).isEqualTo(ccdCourtDetermination.getCourtDecision());
        Assertions.assertThat(actual.getDisposableIncome()).isEqualTo(ccdCourtDetermination.getDisposableIncome());
        return this;
    }
}
