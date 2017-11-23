package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmount;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRange;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;

import static uk.gov.hmcts.cmc.ccd.domain.AmountType.NOT_KNOWN;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.RANGE;

@Component
public class AmountMapper implements Mapper<CCDAmount, Amount> {

    private AmountRangeMapper amountRangeMapper;

    public AmountMapper(final AmountRangeMapper amountRangeMapper) {
        this.amountRangeMapper = amountRangeMapper;
    }

    @Override
    public CCDAmount to(Amount amount) {
        CCDAmount.CCDAmountBuilder builder = CCDAmount.builder();
        if (amount instanceof AmountRange) {
            builder.type(RANGE);
            AmountRange amountRange = (AmountRange) amount;
            builder.amountRange(amountRangeMapper.to(amountRange));
        } else if (amount instanceof NotKnown) {
            builder.type(NOT_KNOWN);
        }

        return builder.build();
    }

    @Override
    public Amount from(CCDAmount ccdAmount) {

        switch (ccdAmount.getType()) {
            case RANGE:
                CCDAmountRange ccdAmountRange = ccdAmount.getAmountRange();
                return amountRangeMapper.from(ccdAmountRange);
            default:
                throw new MappingException();
        }
    }
}
