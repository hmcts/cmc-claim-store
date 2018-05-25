package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;

@Component
public class DebtMapper implements Mapper<CCDDebt, Debt> {

    @Override
    public CCDDebt to(Debt debt) {
        return CCDDebt.builder()
            .description(debt.getDescription())
            .totalOwed(debt.getTotalOwed())
            .monthlyPayments(debt.getMonthlyPayments())
            .build();
    }

    @Override
    public Debt from(CCDDebt ccdDebt) {
        return new Debt(
            ccdDebt.getDescription(),
            ccdDebt.getTotalOwed(),
            ccdDebt.getMonthlyPayments()
        );
    }
}
