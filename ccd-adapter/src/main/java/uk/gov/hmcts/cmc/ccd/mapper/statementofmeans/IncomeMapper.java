package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;

public class IncomeMapper implements Mapper<CCDIncome, Income> {
    @Override
    public CCDIncome to(Income income) {
        return null;
    }

    @Override
    public Income from(CCDIncome ccdIncome) {
        return null;
    }
}
