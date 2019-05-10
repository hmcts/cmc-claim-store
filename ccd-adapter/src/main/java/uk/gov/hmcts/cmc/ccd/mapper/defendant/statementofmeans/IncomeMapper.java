package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncomeType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDPaymentFrequency;
import uk.gov.hmcts.cmc.ccd.mapper.MoneyMapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency;

@Component
public class IncomeMapper {

    private final MoneyMapper moneyMapper;

    @Autowired
    public IncomeMapper(MoneyMapper moneyMapper) {
        this.moneyMapper = moneyMapper;
    }

    public CCDCollectionElement<CCDIncome> to(Income income) {
        if (income == null) {
            return null;
        }

        return CCDCollectionElement.<CCDIncome>builder()
            .value(CCDIncome.builder()
                .type(CCDIncomeType.valueOf(income.getType().name()))
                .amountReceived(moneyMapper.to(income.getAmount()))
                .frequency(CCDPaymentFrequency.valueOf(income.getFrequency().name()))
                .otherSource(income.getOtherSource().orElse(null))
                .build())
            .id(income.getId())
            .build();
    }

    public Income from(CCDCollectionElement<CCDIncome> ccdIncome) {
        CCDIncome value = ccdIncome.getValue();
        if (value == null) {
            return null;
        }

        return Income.builder()
            .id(ccdIncome.getId())
            .amount(moneyMapper.from(value.getAmountReceived()))
            .frequency(PaymentFrequency.valueOf(value.getFrequency().name()))
            .type(Income.IncomeType.valueOf(value.getType().name()))
            .otherSource(value.getOtherSource())
            .build();
    }
}
