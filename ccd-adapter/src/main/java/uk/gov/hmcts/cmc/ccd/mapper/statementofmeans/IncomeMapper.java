package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;

@Component
public class IncomeMapper implements Mapper<CCDIncome, Income> {

    @Override
    public CCDIncome to(Income income) {
        return CCDIncome.builder()
            .type(income.getType())
            .amountReceived(income.getAmountReceived())
            .frequency(income.getFrequency())
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
            .frequency(ccdIncome.getFrequency())
            .type(ccdIncome.getType())
            .otherSource(ccdIncome.getOtherSource())
            .build();
    }
}
