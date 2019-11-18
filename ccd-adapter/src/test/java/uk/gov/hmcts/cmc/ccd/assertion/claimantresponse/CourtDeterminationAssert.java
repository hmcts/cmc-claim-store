package uk.gov.hmcts.cmc.ccd.assertion.claimantresponse;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDCourtDetermination;
import uk.gov.hmcts.cmc.ccd.domain.claimantresponse.CCDDecisionType;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.CourtDetermination;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.DecisionType;

import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertMoney;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class CourtDeterminationAssert extends CustomAssert<CourtDeterminationAssert, CourtDetermination> {
    public CourtDeterminationAssert(CourtDetermination courtDetermination) {
        super("CourtDetermination", courtDetermination, CourtDeterminationAssert.class);
    }

    public CourtDeterminationAssert isEqualTo(CCDCourtDetermination expected) {
        isNotNull();

        compare("rejectionReason",
            expected.getRejectionReason(),
            actual.getRejectionReason());

        compare("courtPaymentIntention",
            expected.getCourtIntention(),
            Optional.ofNullable(actual.getCourtPaymentIntention()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("courtDecision",
            expected.getCourtDecision(),
            Optional.ofNullable(actual.getCourtDecision()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("disposableIncome",
            expected.getDisposableIncome(),
            Optional.ofNullable(actual.getDisposableIncome()),
            (e, a) -> assertMoney(a).isEqualTo(e));

        compare("decisionType",
            expected.getDecisionType(), CCDDecisionType::name,
            Optional.ofNullable(actual.getDecisionType()).map(DecisionType::name));

        return this;
    }
}
