package uk.gov.hmcts.cmc.ccd.mapper.statementofmeans;

import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

public class EmploymentMapper implements Mapper<CCDEmployment, Employment> {
    @Override
    public CCDEmployment to(Employment employment) {
        return null;
    }

    @Override
    public Employment from(CCDEmployment ccdEmployment) {
        return null;
    }
}
