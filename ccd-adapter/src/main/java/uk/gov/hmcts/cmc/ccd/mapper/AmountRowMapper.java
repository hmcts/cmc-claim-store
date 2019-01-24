package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDAmountRow;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

@Component
public class AmountRowMapper {

    public CCDCollectionElement<CCDAmountRow> to(AmountRow amountRow) {
        if (amountRow.getAmount() == null) {
            return null;
        }

        return CCDCollectionElement.<CCDAmountRow>builder()
            .value(CCDAmountRow.builder().reason(amountRow.getReason()).amount(amountRow.getAmount()).build())
            .id(amountRow.getId())
            .build();
    }

    public AmountRow from(CCDCollectionElement<CCDAmountRow> ccdAmountRow) {

        CCDAmountRow value = ccdAmountRow.getValue();
        return new AmountRow(ccdAmountRow.getId(), value.getReason(), value.getAmount());
    }
}
