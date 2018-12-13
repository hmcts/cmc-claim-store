package uk.gov.hmcts.cmc.ccd.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

@Component
public class StatementOfTruthMapper implements BuilderMapper<CCDCase, StatementOfTruth, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(StatementOfTruth statementOfTruth, CCDCase.CCDCaseBuilder builder) {

        builder
            .sotSignerName (statementOfTruth.getSignerName())
            .sotSignerRole(statementOfTruth.getSignerRole());
    }

    @Override
    public StatementOfTruth from(CCDCase ccdCase) {
        if (ccdCase == null) {
            return null;
        }

        return new StatementOfTruth(ccdCase.getSotSignerName(), ccdCase.getSotSignerRole());
    }
}
