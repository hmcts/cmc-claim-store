package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCompany;
import uk.gov.hmcts.cmc.domain.models.party.Company;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class CompanyAssert extends AbstractAssert<CompanyAssert, Company> {

    public CompanyAssert(Company company) {
        super(company, CompanyAssert.class);
    }

    public CompanyAssert isEqualTo(CCDCompany ccdCompany) {
        isNotNull();

        if (!Objects.equals(actual.getName(), ccdCompany.getName())) {
            failWithMessage("Expected CCDCompany.name to be <%s> but was <%s>",
                ccdCompany.getName(), actual.getName());
        }

        if (!Objects.equals(actual.getMobilePhone().orElse(null), ccdCompany.getPhoneNumber())) {
            failWithMessage("Expected CCDCompany.mobilePhone to be <%s> but was <%s>",
                ccdCompany.getPhoneNumber(), actual.getMobilePhone().orElse(null));
        }

        if (!Objects.equals(actual.getContactPerson().orElse(null), ccdCompany.getContactPerson())) {
            failWithMessage("Expected CCDCompany.contactPerson to be <%s> but was <%s>",
                ccdCompany.getContactPerson(), actual.getContactPerson().orElse(null));
        }

        assertThat(ccdCompany.getAddress()).isEqualTo(actual.getAddress());
        actual.getCorrespondenceAddress()
            .ifPresent(address -> assertThat(ccdCompany.getCorrespondenceAddress())
                .isEqualTo(address));
        actual.getRepresentative()
            .ifPresent(representative -> assertThat(ccdCompany.getRepresentative())
                .isEqualTo(representative));

        return this;
    }
}
