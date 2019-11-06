package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;

import java.util.Optional;

public class ReviewOrderAssert extends CustomAssert<ReviewOrderAssert, ReviewOrder> {

    ReviewOrderAssert(ReviewOrder actual) {
        super("ReviewOrder", actual, ReviewOrderAssert.class);
    }

    public ReviewOrderAssert isEqualTo(CCDReviewOrder expected) {
        isNotNull();

        compare("reason",
            expected.getReason(),
            actual.getReason());

        compare("requestedAt",
            expected.getRequestedAt(),
            Optional.ofNullable(actual.getRequestedAt()));

        compare("requestedBy",
            expected.getRequestedBy(), Enum::name,
            Optional.ofNullable(actual.getRequestedBy()).map(Enum::name));

        return this;
    }

}
