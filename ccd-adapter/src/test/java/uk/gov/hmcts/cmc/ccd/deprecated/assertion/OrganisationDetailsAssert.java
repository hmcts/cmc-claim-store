package uk.gov.hmcts.cmc.ccd.deprecated.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDOrganisation;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;

import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.deprecated.assertion.Assertions.assertThat;

public class OrganisationDetailsAssert extends AbstractAssert<OrganisationDetailsAssert, OrganisationDetails> {

    public OrganisationDetailsAssert(OrganisationDetails organisation) {
        super(organisation, OrganisationDetailsAssert.class);
    }

    public OrganisationDetailsAssert isEqualTo(CCDOrganisation ccdOrganisation) {
        isNotNull();

        if (!Objects.equals(actual.getName(), ccdOrganisation.getName())) {
            failWithMessage("Expected CCDOrganisation.name to be <%s> but was <%s>",
                ccdOrganisation.getName(), actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdOrganisation.getEmail())) {
            failWithMessage("Expected CCDOrganisation.email to be <%s> but was <%s>",
                ccdOrganisation.getEmail(), actual.getEmail().orElse(null));
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
        actual.getServiceAddress()
            .ifPresent(address -> assertThat(ccdOrganisation.getCorrespondenceAddress())
                .isEqualTo(address));
        actual.getRepresentative()
            .ifPresent(representative -> assertThat(ccdOrganisation.getRepresentative())
                .isEqualTo(representative));

        return this;
    }
}
