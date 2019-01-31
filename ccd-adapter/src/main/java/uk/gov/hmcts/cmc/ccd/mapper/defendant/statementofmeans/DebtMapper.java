package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;

@Component
public class DebtMapper {

    public CCDCollectionElement<CCDDebt> to(Debt debt) {
        if (debt == null) {
            return null;
        }
        return CCDCollectionElement.<CCDDebt>builder()
            .value(CCDDebt.builder()
                .description(debt.getDescription())
                .totalOwed(debt.getTotalOwed())
                .monthlyPayments(debt.getMonthlyPayments())
                .build())
            .id(debt.getId())
            .build();
    }

    public Debt from(CCDCollectionElement<CCDDebt> collectionElement) {
        CCDDebt ccdDebt = collectionElement.getValue();

        if (ccdDebt == null) {
            return null;
        }

        return Debt.builder()
            .id(collectionElement.getId())
            .description(ccdDebt.getDescription())
            .totalOwed(ccdDebt.getTotalOwed())
            .monthlyPayments(ccdDebt.getMonthlyPayments())
            .build();
    }
}
