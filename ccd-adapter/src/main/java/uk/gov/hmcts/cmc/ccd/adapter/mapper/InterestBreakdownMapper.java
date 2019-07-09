package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;

@Component
public class InterestBreakdownMapper implements BuilderMapper<CCDCase, InterestBreakdown, CCDCase.CCDCaseBuilder> {

    private final MoneyMapper moneyMapper;

    @Autowired
    public InterestBreakdownMapper(MoneyMapper moneyMapper) {
        this.moneyMapper = moneyMapper;
    }

    @Override
    public void to(InterestBreakdown interestBreakdown, CCDCase.CCDCaseBuilder builder) {
        if (interestBreakdown == null) {
            return;
        }

        builder
            .interestBreakDownAmount(moneyMapper.to(interestBreakdown.getTotalAmount()))
            .interestBreakDownExplanation(interestBreakdown.getExplanation());
    }

    @Override
    public InterestBreakdown from(CCDCase ccdCase) {
        if (ccdCase.getInterestBreakDownAmount() == null && ccdCase.getInterestBreakDownExplanation() == null) {
            return null;
        }

        return new InterestBreakdown(
            moneyMapper.from(ccdCase.getInterestBreakDownAmount()),
            ccdCase.getInterestBreakDownExplanation()
        );
    }
}
