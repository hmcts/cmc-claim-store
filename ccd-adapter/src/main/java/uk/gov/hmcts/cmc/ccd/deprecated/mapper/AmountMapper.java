package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmount;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountBreakDown;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDAmountRange;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;

import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.BREAK_DOWN;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.NOT_KNOWN;
import static uk.gov.hmcts.cmc.ccd.deprecated.domain.AmountType.RANGE;

@Component
public class AmountMapper implements Mapper<CCDAmount, Amount> {

    private final AmountRangeMapper amountRangeMapper;
    private final AmountBreakDownMapper amountBreakDownMapper;

    public AmountMapper(AmountRangeMapper amountRangeMapper, AmountBreakDownMapper amountBreakDownMapper) {
        this.amountRangeMapper = amountRangeMapper;
        this.amountBreakDownMapper = amountBreakDownMapper;
    }

    @Override
    public CCDAmount to(Amount amount) {
        CCDAmount.CCDAmountBuilder builder = CCDAmount.builder();
        if (amount instanceof AmountRange) {
            builder.type(RANGE);
            AmountRange amountRange = (AmountRange) amount;
            builder.amountRange(amountRangeMapper.to(amountRange));
        } else if (amount instanceof AmountBreakDown) {
            builder.type(BREAK_DOWN);
            AmountBreakDown amountBreakDown = (AmountBreakDown) amount;
            builder.amountBreakDown(amountBreakDownMapper.to(amountBreakDown));
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
            case NOT_KNOWN:
                return new NotKnown();
            case BREAK_DOWN:
                CCDAmountBreakDown ccdAmountBreakDown = ccdAmount.getAmountBreakDown();
                return amountBreakDownMapper.from(ccdAmountBreakDown);
            default:
                throw new MappingException();
        }
    }
}
