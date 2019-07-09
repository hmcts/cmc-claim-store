package uk.gov.hmcts.cmc.ccd_adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.amount.NotKnown;

import static uk.gov.hmcts.cmc.ccd.domain.AmountType.BREAK_DOWN;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.NOT_KNOWN;
import static uk.gov.hmcts.cmc.ccd.domain.AmountType.RANGE;

@Component
public class AmountMapper implements BuilderMapper<CCDCase, Amount, CCDCase.CCDCaseBuilder> {

    private final AmountRangeMapper amountRangeMapper;
    private final AmountBreakDownMapper amountBreakDownMapper;

    public AmountMapper(AmountRangeMapper amountRangeMapper, AmountBreakDownMapper amountBreakDownMapper) {
        this.amountRangeMapper = amountRangeMapper;
        this.amountBreakDownMapper = amountBreakDownMapper;
    }

    @Override
    public void to(Amount amount, CCDCase.CCDCaseBuilder builder) {
        if (amount instanceof AmountRange) {
            builder.amountType(RANGE);
            AmountRange amountRange = (AmountRange) amount;
            amountRangeMapper.to(amountRange, builder);
        } else if (amount instanceof AmountBreakDown) {
            builder.amountType(BREAK_DOWN);
            AmountBreakDown amountBreakDown = (AmountBreakDown) amount;
            amountBreakDownMapper.to(amountBreakDown, builder);
        } else if (amount instanceof NotKnown) {
            builder.amountType(NOT_KNOWN);
        }
    }

    @Override
    public Amount from(CCDCase ccdCase) {

        switch (ccdCase.getAmountType()) {
            case RANGE:
                return amountRangeMapper.from(ccdCase);
            case NOT_KNOWN:
                return new NotKnown();
            case BREAK_DOWN:
                return amountBreakDownMapper.from(ccdCase);
            default:
                throw new MappingException();
        }
    }
}
