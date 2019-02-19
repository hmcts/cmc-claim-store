package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

@Component
public class AmountRangeMapper implements BuilderMapper<CCDCase, AmountRange, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(AmountRange amountRange, CCDCase.CCDCaseBuilder builder) {
        amountRange.getLowerValue().ifPresent(builder::amountLowerValue);
        builder.amountHigherValue(amountRange.getHigherValue());
    }

    @Override
    public AmountRange from(CCDCase ccdAmountRange) {
        return new AmountRange(ccdAmountRange.getAmountLowerValue(), ccdAmountRange.getAmountHigherValue());
    }
}
