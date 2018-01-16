package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.util.stream.Collectors;

@Component
public class AmountBreakDownMapper implements Mapper<CCDAmountBreakDown, AmountBreakDown> {

    private final AmountRowMapper amountRowMapper;

    public AmountBreakDownMapper(AmountRowMapper amountRowMapper) {
        this.amountRowMapper = amountRowMapper;
    }

    @Override
    public CCDAmountBreakDown to(AmountBreakDown amountBreakDown) {
        CCDAmountBreakDown.CCDAmountBreakDownBuilder builder = CCDAmountBreakDown.builder();
        builder.rows(amountBreakDown.getRows().stream().map(amountRowMapper::to).collect(Collectors.toList()));
        return builder.build();
    }

    @Override
    public AmountBreakDown from(CCDAmountBreakDown ccdAmountBreakDown) {
        return new AmountBreakDown(
            ccdAmountBreakDown.getRows().stream().map(amountRowMapper::from).collect(Collectors.toList())
        );
    }
}
