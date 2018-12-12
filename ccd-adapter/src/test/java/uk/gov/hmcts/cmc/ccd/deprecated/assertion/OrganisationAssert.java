package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class OrganisationAssert extends AbstractAssert<OrganisationAssert, Organisation> {

    public OrganisationAssert(Organisation organisation) {
        super(organisation, OrganisationAssert.class);
    }

    public OrganisationAssert isEqualTo(CCDOrganisation ccdOrganisation) {
        isNotNull();

        if (!Objects.equals(actual.getName(), ccdOrganisation.getName())) {
            failWithMessage("Expected CCDOrganisation.name to be <%s> but was <%s>",
                ccdOrganisation.getName(), actual.getName());
        }

        if (!Objects.equals(actual.getMobilePhone().orElse(null), ccdOrganisation.getPhoneNumber())) {
            failWithMessage("Expected CCDOrganisation.mobilePhone to be <%s> but was <%s>",
                ccdOrganisation.getPhoneNumber(), actual.getMobilePhone().orElse(null));
        }

        if (!Objects.equals(actual.getContactPerson().orElse(null), ccdOrganisation.getContactPerson())) {
            failWithMessage("Expected CCDSoleTrader.contactPerson to be <%s> but was <%s>",
                ccdOrganisation.getContactPerson(), actual.getContactPerson().orElse(null));
        }

        if (!Objects.equals(actual.getCompaniesHouseNumber().orElse(null),
            ccdOrganisation.getCompaniesHouseNumber())) {

            failWithMessage("Expected CCDSoleTrader.contactPerson to be <%s> but was <%s>",
                ccdOrganisation.getCompaniesHouseNumber(), actual.getCompaniesHouseNumber().orElse(null));
        }

        assertThat(ccdOrganisation.getAddress()).isEqualTo(actual.getAddress());
        actual.getCorrespondenceAddress()
            .ifPresent(address -> assertThat(ccdOrganisation.getCorrespondenceAddress())
                .isEqualTo(address));
        actual.getRepresentative()
            .ifPresent(representative -> assertThat(ccdOrganisation.getRepresentative())
                .isEqualTo(representative));

        return this;
    }
}
