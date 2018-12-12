package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

//@Component
public class AmountRangeMapper implements Mapper<CCDAmountRange, AmountRange> {

    @Override
    public CCDAmountRange to(AmountRange amountRange) {
        CCDAmountRange.CCDAmountRangeBuilder builder = CCDAmountRange.builder();
        amountRange.getLowerValue().ifPresent(builder::lowerValue);
        return builder.higherValue(amountRange.getHigherValue()).build();
    }

    @Override
    public AmountRange from(CCDAmountRange ccdAmountRange) {
        return new AmountRange(ccdAmountRange.getLowerValue(), ccdAmountRange.getHigherValue());
    }
}
