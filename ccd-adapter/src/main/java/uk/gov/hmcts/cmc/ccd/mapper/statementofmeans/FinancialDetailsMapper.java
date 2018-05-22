package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDFinancialDetails;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.FinancialDetails;

public class FinancialDetailsMapper implements Mapper<CCDFinancialDetails, FinancialDetails> {
    @Override
    public CCDFinancialDetails to(FinancialDetails financialDetails) {
        return null;
    }

    @Override
    public FinancialDetails from(CCDFinancialDetails ccdFinancialDetails) {
        return null;
    }
}
