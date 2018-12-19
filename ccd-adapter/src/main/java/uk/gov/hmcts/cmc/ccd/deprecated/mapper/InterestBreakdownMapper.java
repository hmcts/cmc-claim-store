package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDInterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

@Component
public class InterestBreakdownMapper implements Mapper<CCDInterestBreakdown, InterestBreakdown> {
    @Override
    public CCDInterestBreakdown to(InterestBreakdown interestBreakdown) {
        if (interestBreakdown == null) {
            return null;
        }

        return CCDInterestBreakdown.builder()
            .totalAmount(interestBreakdown.getTotalAmount())
            .explanation(interestBreakdown.getExplanation())
            .build();
    }

    @Override
    public InterestBreakdown from(CCDInterestBreakdown ccdInterestBreakdown) {
        if (ccdInterestBreakdown == null) {
            return null;
        }

        return new InterestBreakdown(
            ccdInterestBreakdown.getTotalAmount(),
            ccdInterestBreakdown.getExplanation()
        );
    }
}
