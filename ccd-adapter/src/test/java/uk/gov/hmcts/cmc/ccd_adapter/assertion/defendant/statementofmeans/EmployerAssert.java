package uk.gov.hmcts.cmc.ccd_adapter.assertion.defendant.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;

import java.util.Objects;

public class EmployerAssert extends AbstractAssert<EmployerAssert, Employer> {

    public EmployerAssert(Employer actual) {
        super(actual, EmployerAssert.class);
    }

    public EmployerAssert isEqualTo(CCDEmployer ccdEmployer) {
        isNotNull();

        if (!Objects.equals(actual.getName(), ccdEmployer.getEmployerName())) {
            failWithMessage("Expected Employer.name to be <%s> but was <%s>",
                ccdEmployer.getEmployerName(), actual.getName());
        }

        if (!Objects.equals(actual.getJobTitle(), ccdEmployer.getJobTitle())) {
            failWithMessage("Expected Employer.jobTitle to be <%s> but was <%s>",
                ccdEmployer.getJobTitle(), actual.getJobTitle());
        }

        return this;
    }
}
