package uk.gov.hmcts.cmc.ccd.assertion.defendant.statementofmeans;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans.CCDEmployer;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employer;

import java.util.Optional;

public class EmployerAssert extends CustomAssert<EmployerAssert, Employer> {

    public EmployerAssert(Employer actual) {
        super("Employer", actual, EmployerAssert.class);
    }

    public EmployerAssert isEqualTo(CCDEmployer expected) {
        isNotNull();

        compare("name",
            expected.getEmployerName(),
            Optional.ofNullable(actual.getName()));

        compare("jobTitle",
            expected.getJobTitle(),
            Optional.ofNullable(actual.getJobTitle()));

        return this;
    }
}
