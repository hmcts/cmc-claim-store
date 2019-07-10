package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;

import java.util.Objects;

public class ReviewOrderAssert extends AbstractAssert<ReviewOrderAssert, ReviewOrder> {

    public ReviewOrderAssert(ReviewOrder actual) {
        super(actual, ReviewOrderAssert.class);
    }

    public ReviewOrderAssert isEqualTo(CCDReviewOrder ccdReviewOrder) {
        isNotNull();

        if (!Objects.equals(actual.getReason().orElse(null), ccdReviewOrder.getReason())) {
            failWithMessage("Expected ReviewOrder.reason to be <%s> but was <%s>",
                ccdReviewOrder.getReason(), actual.getReason());
        }

        if (!Objects.equals(actual.getRequestedAt(), ccdReviewOrder.getRequestedAt())) {
            failWithMessage("Expected ReviewOrder.requestedAt to be <%s> but was <%s>",
                ccdReviewOrder.getRequestedAt(), actual.getRequestedAt());
        }

        if (!Objects.equals(actual.getRequestedBy().name(), ccdReviewOrder.getRequestedBy().name())) {
            failWithMessage("Expected ReviewOrder.requestedBy to be <%s> but was <%s>",
                ccdReviewOrder.getRequestedBy(), actual.getRequestedBy());
        }

        return this;
    }

}
