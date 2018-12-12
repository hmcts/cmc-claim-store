package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
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
        builder.amountBreakDown(amountBreakDown.getRows().stream().map(amountRowMapper::to)
            .filter(Objects::nonNull)
            .map(row -> CCDCollectionElement.<CCDAmountRow>builder().value(row).build())
            .collect(Collectors.toList()));

    }

    @Override
    public AmountBreakDown from(CCDCase ccdAmountBreakDown) {
        return new AmountBreakDown(
            ccdAmountBreakDown.getAmountBreakDown().stream()
                .map(CCDCollectionElement::getValue)
                .map(amountRowMapper::from)
                .collect(Collectors.toList())
        );
    }
}
