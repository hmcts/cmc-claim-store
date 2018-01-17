package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountBreakDown;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AmountBreakDownMapper implements Mapper<CCDAmountBreakDown, AmountBreakDown> {
    private static final String COLLECTION_KEY_NAME = "value";
    private final AmountRowMapper amountRowMapper;

    public AmountBreakDownMapper(AmountRowMapper amountRowMapper) {
        this.amountRowMapper = amountRowMapper;
    }

    @Override
    public CCDAmountBreakDown to(AmountBreakDown amountBreakDown) {
        CCDAmountBreakDown.CCDAmountBreakDownBuilder builder = CCDAmountBreakDown.builder();
        builder.rows(amountBreakDown.getRows().stream().map(amountRowMapper::to)
            .filter(Objects::nonNull)
            .map(this::mapToValue)
            .collect(Collectors.toList()));

        return builder.build();
    }

    @Override
    public AmountBreakDown from(CCDAmountBreakDown ccdAmountBreakDown) {
        return new AmountBreakDown(
            ccdAmountBreakDown.getRows().stream()
                .map(this::valueFromMap)
                .map(amountRowMapper::from)
                .collect(Collectors.toList())
        );
    }

    private Map<String, CCDAmountRow> mapToValue(CCDAmountRow ccdParty) {
        return Collections.singletonMap(COLLECTION_KEY_NAME, ccdParty);
    }

    private CCDAmountRow valueFromMap(Map<String, CCDAmountRow> value) {
        return value.get(COLLECTION_KEY_NAME);
    }

}
