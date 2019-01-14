package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

@Component
public class InterestBreakdownMapper implements BuilderMapper<CCDCase, InterestBreakdown, CCDCase.CCDCaseBuilder> {
    @Override
    public void to(InterestBreakdown interestBreakdown, CCDCase.CCDCaseBuilder builder) {
        if (interestBreakdown == null) {
            return;
        }

        builder
            .interestBreakDownAmount(interestBreakdown.getTotalAmount())
            .interestBreakDownExplanation(interestBreakdown.getExplanation());
    }

    @Override
    public InterestBreakdown from(CCDCase ccdCase) {
        if (ccdCase.getInterestBreakDownAmount() == null && ccdCase.getInterestBreakDownExplanation() == null) {
            return null;
        }

        return new InterestBreakdown(
            ccdCase.getInterestBreakDownAmount(),
            ccdCase.getInterestBreakDownExplanation()
        );
    }
}
