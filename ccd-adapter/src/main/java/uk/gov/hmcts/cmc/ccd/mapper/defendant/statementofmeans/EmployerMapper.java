package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;

@Component
public class EmployerMapper implements Mapper<CCDEmployer, Employer> {

    @Override
    public CCDEmployer to(Employer employer) {
        return CCDEmployer.builder()
            .name(employer.getName())
            .jobTitle(employer.getJobTitle())
            .build();
    }

    @Override
    public Employer from(CCDEmployer ccdEmployer) {
        return new Employer(
            ccdEmployer.getJobTitle(),
            ccdEmployer.getName()
        );
    }
}
