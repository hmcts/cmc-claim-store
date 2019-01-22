package uk.gov.hmcts.cmc.ccd.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;

@Component
public class EmployerMapper {

    public CCDEmployer to(Employer employer) {
        return CCDEmployer.builder()
            .employerName(employer.getName())
            .jobTitle(employer.getJobTitle())
            .build();
    }

    public Employer from(CCDCollectionElement<CCDEmployer> ccdEmployer) {
        CCDEmployer value = ccdEmployer.getValue();
        if (value == null) {
            return null;
        }

        return Employer.builder()
            .id(ccdEmployer.getId())
            .jobTitle(value.getJobTitle())
            .name(value.getEmployerName())
            .build();

    }
}
