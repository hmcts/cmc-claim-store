package uk.gov.hmcts.cmc.ccd.deprecated.mapper;

import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

//@Component
public class StatementOfTruthMapper implements Mapper<CCDStatementOfTruth, StatementOfTruth> {

    @Override
    public CCDStatementOfTruth to(StatementOfTruth statementOfTruth) {

        return CCDStatementOfTruth.builder()
            .signerName(statementOfTruth.getSignerName())
            .signerRole(statementOfTruth.getSignerRole())
            .build();
    }

    @Override
    public StatementOfTruth from(CCDStatementOfTruth statementOfTruth) {
        if (statementOfTruth == null) {
            return null;
        }

        return new StatementOfTruth(statementOfTruth.getSignerName(), statementOfTruth.getSignerRole());
    }
}
