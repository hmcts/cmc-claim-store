package uk.gov.hmcts.cmc.ccd.deprecated.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDEmployment;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.statementofmeans.CCDUnemployed;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class EmploymentAssert extends AbstractAssert<EmploymentAssert, Employment> {

    public EmploymentAssert(Employment actual) {
        super(actual, EmploymentAssert.class);
    }

    public EmploymentAssert isEqualTo(CCDEmployment ccdEmployment) {
        isNotNull();

        actual.getSelfEmployment()
            .ifPresent(selfEmployed -> assertThat(selfEmployed).isEqualTo(ccdEmployment.getSelfEmployment()));

        if (!Objects.equals(actual.getEmployers().size(), ccdEmployment.getEmployers().size())) {
            failWithMessage("Expected Employment.size to be <%s> but was <%s>",
                actual.getEmployers().size(), ccdEmployment.getEmployers().size());
        }

        actual.getEmployers()
            .forEach(employer -> assertEmployerElement(employer, ccdEmployment.getEmployers()));

        actual.getUnemployment().ifPresent(unemployment -> {
            unemployment.getUnemployed().ifPresent(unemployed -> {
                CCDUnemployed ccdUnemployed = ccdEmployment.getUnemployment().getUnemployed();
                if (!Objects.equals(unemployed.getNumberOfYears(),
                    ccdUnemployed.getNumberOfYears())) {
                    failWithMessage("Expected Unemployed.numberOfYears to be <%s> but was <%s>",
                        unemployed.getNumberOfYears(), ccdUnemployed.getNumberOfYears());
                }
            });
        });
        return this;
    }

    private void assertEmployerElement(
        Employer actual,
        List<CCDCollectionElement<CCDEmployer>> ccdEmployers
    ) {
        ccdEmployers.stream()
            .map(CCDCollectionElement::getValue)
            .filter(employer -> actual.getName().equals(employer.getName()))
            .findFirst()
            .ifPresent(employer -> assertThat(actual).isEqualTo(employer));
    }
}
