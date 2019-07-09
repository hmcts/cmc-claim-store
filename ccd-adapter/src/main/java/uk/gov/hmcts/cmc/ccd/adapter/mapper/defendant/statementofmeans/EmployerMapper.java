package uk.gov.hmcts.cmc.ccd.adapter.mapper.defendant.statementofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;

@Component
public class EmployerMapper {

    public CCDCollectionElement<CCDEmployer> to(Employer employer) {
        if (employer == null) {
            return null;
        }
        return CCDCollectionElement.<CCDEmployer>builder()
            .value(CCDEmployer.builder()
                .employerName(employer.getName())
                .jobTitle(employer.getJobTitle())
                .build())
            .id(employer.getId())
            .build();
    }

    public Employer from(CCDCollectionElement<CCDEmployer> collectionElement) {
        CCDEmployer ccdEmployer = collectionElement.getValue();
        if (ccdEmployer == null) {
            return null;
        }

        return Employer.builder()
            .id(collectionElement.getId())
            .jobTitle(ccdEmployer.getJobTitle())
            .name(ccdEmployer.getEmployerName())
            .build();

    }
}
