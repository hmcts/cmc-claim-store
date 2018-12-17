package uk.gov.hmcts.cmc.ccd.mapper.defendant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

@Component
public class StatementOfMeansMapper implements Mapper<CCDStatementOfMeans, StatementOfMeans> {

    @Override
    public CCDStatementOfMeans to(StatementOfMeans statementOfMeans) {
        return null;
    }

    @Override
    public StatementOfMeans from(CCDStatementOfMeans ccdStatementOfMeans) {
        return null;
    }
}
