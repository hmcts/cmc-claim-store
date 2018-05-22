package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;

public class DebtMapper implements Mapper<CCDDebt, Debt> {
    @Override
    public CCDDebt to(Debt debt) {
        return null;
    }

    @Override
    public Debt from(CCDDebt ccdDebt) {
        return null;
    }
}
