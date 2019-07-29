package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDReviewOrder;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;

@Component
public class ReviewOrderMapper implements Mapper<CCDReviewOrder, ReviewOrder> {

    @Override
    public CCDReviewOrder to(ReviewOrder reviewOrder) {
        if (reviewOrder == null) {
            return null;
        }

        return CCDReviewOrder.builder()
            .requestedAt(reviewOrder.getRequestedAt())
            .reason(reviewOrder.getReason().orElse(null))
            .requestedBy(CCDReviewOrder.RequestedBy.valueOf(reviewOrder.getRequestedBy().name()))
            .build();
    }

    @Override
    public ReviewOrder from(CCDReviewOrder ccdReviewOrder) {
        if (ccdReviewOrder == null) {
            return null;
        }

        return ReviewOrder.builder()
            .reason(ccdReviewOrder.getReason())
            .requestedAt(ccdReviewOrder.getRequestedAt())
            .requestedBy(ReviewOrder.RequestedBy.valueOf(ccdReviewOrder.getRequestedBy().name()))
            .build();
    }
}
