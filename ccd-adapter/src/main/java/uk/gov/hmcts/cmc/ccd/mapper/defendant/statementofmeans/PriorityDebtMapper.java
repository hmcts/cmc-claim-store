package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebt;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPriorityDebtType;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PriorityDebt;

@Component
public class PriorityDebtMapper {

    public CCDCollectionElement<CCDPriorityDebt> to(PriorityDebt priorityDebt) {
        if (priorityDebt == null) {
            return null;
        }

        return CCDCollectionElement.<CCDPriorityDebt>builder()
            .value(CCDPriorityDebt.builder()
                .amount(priorityDebt.getAmount())
                .type(CCDPriorityDebtType.valueOf(priorityDebt.getType().name()))
                .frequency(CCDPaymentFrequency.valueOf(priorityDebt.getFrequency().name()))
                .build())
            .id(priorityDebt.getId())
            .build();
    }

    public PriorityDebt from(CCDCollectionElement<CCDPriorityDebt> collectionElement) {
        CCDPriorityDebt ccdPriorityDebt = collectionElement.getValue();

        if (ccdPriorityDebt == null) {
            return null;
        }

        return PriorityDebt.builder()
            .id(collectionElement.getId())
            .amount(ccdPriorityDebt.getAmount())
            .frequency(PaymentFrequency.valueOf(ccdPriorityDebt.getFrequency().name()))
            .type(PriorityDebt.PriorityDebtType.valueOf(ccdPriorityDebt.getType().name()))
            .build();
    }
}
