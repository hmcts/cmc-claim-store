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
            .type(income.getType())
            .amountReceived(income.getAmountReceived())
            .frequency(CCDPaymentFrequency.valueOf(income.getFrequency().name()))
            .build();
    }

    @Override
    public Income from(CCDIncome ccdIncome) {
        return new Income(
            ccdIncome.getType(),
            PaymentFrequency.valueOf(ccdIncome.getFrequency().name()),
            ccdIncome.getAmountReceived()
        );
    }
}
