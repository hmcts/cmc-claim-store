package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCompany;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class CompanyDetailsAssert extends AbstractAssert<CompanyDetailsAssert, CompanyDetails> {

    public CompanyDetailsAssert(CompanyDetails company) {
        super(company, CompanyDetailsAssert.class);
    }

    public CompanyDetailsAssert isEqualTo(CCDCompany ccdCompany) {
        isNotNull();

        if (!Objects.equals(actual.getName(), ccdCompany.getName())) {
            failWithMessage("Expected CCDCompany.name to be <%s> but was <%s>",
                ccdCompany.getName(), actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdCompany.getEmail())) {
            failWithMessage("Expected CCDCompany.email to be <%s> but was <%s>",
                ccdCompany.getEmail(), actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getContactPerson().orElse(null), ccdCompany.getContactPerson())) {
            failWithMessage("Expected CCDCompany.contactPerson to be <%s> but was <%s>",
                ccdCompany.getContactPerson(), actual.getContactPerson().orElse(null));
        }

        assertThat(ccdCompany.getAddress()).isEqualTo(actual.getAddress());
        actual.getServiceAddress()
            .ifPresent(address -> assertThat(ccdCompany.getCorrespondenceAddress())
                .isEqualTo(address));
        actual.getRepresentative()
            .ifPresent(representative -> assertThat(ccdCompany.getRepresentative())
                .isEqualTo(representative));

        return this;
    }
}
