package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

@Component
public class IncomeMapper implements Mapper<CCDIncome, Income> {

    @Override
    public CCDIncome to(Income income) {
        return CCDIncome.builder()
            .type(CCDIncome.IncomeType.valueOf(income.getType().name()))
            .amountReceived(income.getAmountReceived())
            .frequency(CCDPaymentFrequency.valueOf(income.getFrequency().name()))
            .otherSource(income.getOtherSource().orElse(null))
            .build();
    }

    @Override
    public Income from(CCDIncome ccdIncome) {
        if (ccdIncome == null) {
            return null;
        }

        return Income.builder()
            .amountReceived(ccdIncome.getAmountReceived())
            .frequency(PaymentFrequency.valueOf(ccdIncome.getFrequency().name()))
            .type(Income.IncomeType.valueOf(ccdIncome.getType().name()))
            .otherSource(ccdIncome.getOtherSource())
            .build();
    }
}
