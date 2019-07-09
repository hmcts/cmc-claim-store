package uk.gov.hmcts.cmc.ccd_adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AmountBreakDownMapper implements BuilderMapper<CCDCase, AmountBreakDown, CCDCase.CCDCaseBuilder> {
    private final AmountRowMapper amountRowMapper;

    public AmountBreakDownMapper(AmountRowMapper amountRowMapper) {
        this.amountRowMapper = amountRowMapper;
    }

    @Override
    public void to(AmountBreakDown amountBreakDown, CCDCase.CCDCaseBuilder builder) {
        builder.amountBreakDown(amountBreakDown.getRows().stream()
            .map(amountRowMapper::to)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));

    }

    @Override
    public AmountBreakDown from(CCDCase ccdCase) {
        return new AmountBreakDown(
            ccdCase.getAmountBreakDown().stream()
                .map(amountRowMapper::from)
                .collect(Collectors.toList())
        );
    }
}
