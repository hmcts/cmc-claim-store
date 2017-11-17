package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.StatementOfTruth;

@Component
public class StatementOfTruthMapper
    implements Mapper<StatementOfTruth, uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth> {

    @Override
    public StatementOfTruth to(uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth statementOfTruth) {

        return StatementOfTruth.builder()
            .signerName(statementOfTruth.getSignerName())
            .signerRole(statementOfTruth.getSignerRole())
            .build();
    }

    @Override
    public uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth from(StatementOfTruth statementOfTruth) {

        return new uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth(statementOfTruth.getSignerName(),
            statementOfTruth.getSignerRole());
    }
}
