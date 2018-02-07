package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDStatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

@Component
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
