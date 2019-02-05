package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;

@Component
public class IncomeMapper {

    public CCDCollectionElement<CCDIncome> to(Income income) {
        if (income == null) {
            return null;
        }

        return CCDCollectionElement.<CCDIncome>builder()
            .value(CCDIncome.builder()
                .type(income.getType())
                .amountReceived(income.getAmount())
                .frequency(income.getFrequency())
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
            .amount(value.getAmountReceived())
            .frequency(value.getFrequency())
            .type(value.getType())
            .otherSource(value.getOtherSource())
            .build();
    }
}
