package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

@Component
public class AmountRowMapper implements Mapper<CCDAmountRow, AmountRow> {

    @Override
    public CCDAmountRow to(AmountRow amountRow) {
        if (amountRow.getAmount() == null) {
            return null;
        }

        CCDAmountRow.CCDAmountRowBuilder builder = CCDAmountRow.builder();
        return builder.reason(amountRow.getReason()).amount(amountRow.getAmount()).build();
    }

    @Override
    public AmountRow from(CCDAmountRow ccdAmountRow) {
        return new AmountRow(ccdAmountRow.getReason(), ccdAmountRow.getAmount());
    }
}
