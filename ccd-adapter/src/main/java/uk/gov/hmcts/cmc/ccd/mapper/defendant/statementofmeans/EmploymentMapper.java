package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.deprecated.mapper.Mapper;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;

@Component
public class EmploymentMapper implements Mapper<CCDEmployment, Employer> {

    @Override
    public CCDEmployment to(Employer employer) {
        return CCDEmployment.builder()
            .employerName(employer.getName())
            .jobTitle(employer.getJobTitle())
            .build();
    }

    @Override
    public Employer from(CCDEmployment ccdEmployer) {
        return new Employer(
            ccdEmployer.getJobTitle(),
            ccdEmployer.getEmployerName()
        );
    }
}
