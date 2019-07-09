package uk.gov.hmcts.cmc.ccd.adapter.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class StatementOfTruthCaseMapper implements BuilderMapper<CCDCase, StatementOfTruth, CCDCase.CCDCaseBuilder> {

    @Override
    public void to(StatementOfTruth statementOfTruth, CCDCase.CCDCaseBuilder builder) {

        builder
            .sotSignerName(statementOfTruth.getSignerName())
            .sotSignerRole(statementOfTruth.getSignerRole());
    }

    @Override
    public StatementOfTruth from(CCDCase ccdCase) {
        if (isBlank(ccdCase.getSotSignerName()) && isBlank(ccdCase.getSotSignerRole())) {
            return null;
        }

        return new StatementOfTruth(ccdCase.getSotSignerName(), ccdCase.getSotSignerRole());
    }
}
