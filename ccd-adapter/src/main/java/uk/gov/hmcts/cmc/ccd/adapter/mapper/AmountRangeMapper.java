package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

@Component
public class AmountRangeMapper implements BuilderMapper<CCDCase, AmountRange, CCDCase.CCDCaseBuilder> {

    private final MoneyMapper moneyMapper;

    public AmountRangeMapper(MoneyMapper moneyMapper) {
        this.moneyMapper = moneyMapper;
    }

    @Override
    public void to(AmountRange amountRange, CCDCase.CCDCaseBuilder builder) {
        amountRange.getLowerValue().map(moneyMapper::to).ifPresent(builder::amountLowerValue);
        builder.amountHigherValue(moneyMapper.to(amountRange.getHigherValue()));
    }

    @Override
    public AmountRange from(CCDCase ccdAmountRange) {
        return AmountRange.builder()
            .lowerValue(moneyMapper.from(ccdAmountRange.getAmountLowerValue()))
            .higherValue(moneyMapper.from(ccdAmountRange.getAmountHigherValue()))
            .build();
    }
}
