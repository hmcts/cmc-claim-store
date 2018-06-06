package uk.gov.hmcts.cmc.ccd.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class EmploymentAssert extends AbstractAssert<EmploymentAssert, Employment> {

    public EmploymentAssert(Employment actual) {
        super(actual, EmploymentAssert.class);
    }

    public EmploymentAssert isEqualTo(CCDEmployment ccdEmployment) {
        isNotNull();

        if (!Objects.equals(actual.getEmploymentOption().name(), ccdEmployment.getIsEmployed().name())) {
            failWithMessage("Expected Employment.getEmploymentOption to be <%s> but was <%s>",
                ccdEmployment.getIsEmployed(), actual.getEmploymentOption());
        }

        if (!Objects.equals(actual.getSelfEmployedOption().name(), ccdEmployment.getIsSelfEmployed().name())) {
            failWithMessage("Expected Employment.getSelfEmployedOption to be <%s> but was <%s>",
                ccdEmployment.getIsSelfEmployed().name(), actual.getSelfEmployedOption().name());
        }

        actual.getSelfEmployed()
            .ifPresent(selfEmployed -> assertThat(selfEmployed).isEqualTo(ccdEmployment.getSelfEmployed()));

        if (!Objects.equals(actual.getEmployers().size(), ccdEmployment.getEmployers().size())) {
            failWithMessage("Expected Employment.size to be <%s> but was <%s>",
                actual.getEmployers().size(), ccdEmployment.getEmployers().size());
        }

        actual.getEmployers()
            .forEach(employer -> assertEmployerElement(employer, ccdEmployment.getEmployers()));

        return this;
    }

    private void assertEmployerElement(
        Employer actual,
        List<CCDCollectionElement<CCDEmployer>> ccdEmployers
    ) {
        ccdEmployers.stream()
            .map(CCDCollectionElement::getValue)
            .filter(employer -> actual.getEmployerName().equals(employer.getEmployerName()))
            .findFirst()
            .ifPresent(employer -> assertThat(actual).isEqualTo(employer));
    }
}
