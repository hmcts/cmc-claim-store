package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import java.util.Objects;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;

public class DefendantAssert extends AbstractAssert<DefendantAssert, TheirDetails> {

    public DefendantAssert(TheirDetails party) {
        super(party, DefendantAssert.class);
    }

    public DefendantAssert isEqualTo(CCDDefendant ccdParty) {
        isNotNull();

        if (actual instanceof IndividualDetails) {
            if (!Objects.equals(INDIVIDUAL, ccdParty.getPartyType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getPartyType(), INDIVIDUAL);
            }

            IndividualDetails actual = (IndividualDetails) this.actual;

            assertThat((actual).getAddress()).isEqualTo(ccdParty.getPartyAddress());
            if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
                failWithMessage("Expected CCDIndividual.name to be <%s> but was <%s>",
                    ccdParty.getPartyName(), actual.getName());
            }

            if (!Objects.equals(actual.getEmail().orElse(null), ccdParty.getPartyEmail())) {
                failWithMessage("Expected CCDIndividual.email to be <%s> but was <%s>",
                    ccdParty.getPartyEmail(), actual.getEmail().orElse(null));
            }
            actual.getDateOfBirth()
                .ifPresent(dob -> assertThat(dob.format(ISO_DATE)).isEqualTo(ccdParty.getPartyDateOfBirth()));

            actual.getServiceAddress().ifPresent(address ->
                assertThat(ccdParty.getPartyServiceAddress()).isEqualTo(address)
            );

            actual.getRepresentative()
                .ifPresent(representative -> assertRespresentativeDetails(representative, ccdParty));
        }

        if (actual instanceof OrganisationDetails) {
            if (!Objects.equals(ORGANISATION, ccdParty.getPartyType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getPartyType(), ORGANISATION);
            }

            OrganisationDetails actual = (OrganisationDetails) this.actual;

            assertThat((actual).getAddress()).isEqualTo(ccdParty.getPartyAddress());
            if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
                failWithMessage("Expected CCDOrganisation.name to be <%s> but was <%s>",
                    ccdParty.getPartyName(), actual.getName());
            }

            if (!Objects.equals(actual.getEmail().orElse(null), ccdParty.getPartyEmail())) {
                failWithMessage("Expected CCDOrganisation.email to be <%s> but was <%s>",
                    ccdParty.getPartyEmail(), actual.getEmail().orElse(null));
            }

            String contactPerson = actual.getContactPerson().orElse(null);
            if (!Objects.equals(contactPerson, ccdParty.getPartyContactPerson())) {
                failWithMessage("Expected CCDOrganisation.contactPerson to be <%s> but was <%s>",
                    ccdParty.getPartyContactPerson(), contactPerson);
            }

            String companyHouseNumber = actual.getCompaniesHouseNumber().orElse(null);

            if (!Objects.equals(companyHouseNumber, ccdParty.getPartyCompaniesHouseNumber())) {
                failWithMessage("Expected CCDOrganisation.companyHouseNumber to be <%s> but was <%s>",
                    ccdParty.getPartyCompaniesHouseNumber(), companyHouseNumber);
            }

            actual.getServiceAddress().ifPresent(address ->
                assertThat(ccdParty.getPartyServiceAddress()).isEqualTo(address)
            );

            actual.getRepresentative()
                .ifPresent(representative -> assertRespresentativeDetails(representative, ccdParty));
        }

        if (actual instanceof CompanyDetails) {
            if (!Objects.equals(COMPANY, ccdParty.getPartyType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getPartyType(), COMPANY);
            }

            CompanyDetails actual = (CompanyDetails) this.actual;

            assertThat(actual.getAddress()).isEqualTo(ccdParty.getPartyAddress());
            if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
                failWithMessage("Expected CCDCompanyDetails.name to be <%s> but was <%s>",
                    ccdParty.getPartyName(), actual.getName());
            }

            if (!Objects.equals(actual.getEmail().orElse(null), ccdParty.getPartyEmail())) {
                failWithMessage("Expected CCDCompanyDetails.email to be <%s> but was <%s>",
                    ccdParty.getPartyEmail(), actual.getEmail().orElse(null));
            }

            if (!Objects.equals(actual.getContactPerson().orElse(null), ccdParty.getPartyContactPerson())) {
                failWithMessage("Expected CCDCompany.contactPerson to be <%s> but was <%s>",
                    ccdParty.getPartyContactPerson(), actual.getContactPerson().orElse(null));
            }

            actual.getServiceAddress().ifPresent(address ->
                assertThat(ccdParty.getPartyServiceAddress()).isEqualTo(address)
            );

            actual.getRepresentative()
                .ifPresent(representative -> assertRespresentativeDetails(representative, ccdParty));
        }

        if (actual instanceof SoleTraderDetails) {
            if (!Objects.equals(SOLE_TRADER, ccdParty.getPartyType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getPartyType(), SOLE_TRADER);
            }
            SoleTraderDetails actual = (SoleTraderDetails) this.actual;
            assertThat(actual.getAddress()).isEqualTo(ccdParty.getPartyAddress());

            actual.getTitle().ifPresent(title -> assertThat(ccdParty.getPartyTitle()).isEqualTo(title));

            if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
                failWithMessage("Expected CCDSoleTrader.name to be <%s> but was <%s>",
                    ccdParty.getPartyName(), this.actual.getName());
            }

            if (!Objects.equals(actual.getEmail().orElse(null), ccdParty.getPartyEmail())) {
                failWithMessage("Expected CCDSoleTrader.email to be <%s> but was <%s>",
                    ccdParty.getPartyEmail(), this.actual.getEmail().orElse(null));
            }

            if (!Objects.equals(actual.getBusinessName().orElse(null), ccdParty.getPartyBusinessName())) {
                failWithMessage("Expected CCDSoleTrader.businessName to be <%s> but was <%s>",
                    ccdParty.getPartyBusinessName(), actual.getBusinessName().orElse(null));
            }

            actual.getServiceAddress().ifPresent(address ->
                assertThat(ccdParty.getPartyServiceAddress()).isEqualTo(address)
            );

            actual.getRepresentative()
                .ifPresent(representative -> assertRespresentativeDetails(representative, ccdParty));

        }

        return this;
    }

    private void assertRespresentativeDetails(Representative representative, CCDDefendant ccdParty) {
        if (!Objects.equals(representative.getOrganisationName(), ccdParty.getRepresentativeOrganisationName())) {
            failWithMessage("Expected Representative.organisationName to be <%s> but was <%s>",
                ccdParty.getRepresentativeOrganisationName(), representative.getOrganisationName());
        }

        assertThat(representative.getOrganisationAddress())
            .isEqualTo(ccdParty.getRepresentativeOrganisationAddress());

        representative.getOrganisationContactDetails().ifPresent(contactDetails -> {

            contactDetails.getDxAddress().ifPresent(dxAddress -> {
                if (!Objects.equals(dxAddress, ccdParty.getRepresentativeOrganisationDxAddress())) {
                    failWithMessage("Expected Representative.organisationDxAddress to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationDxAddress(), contactDetails.getDxAddress());
                }
            });

            contactDetails.getEmail().ifPresent(email -> {
                if (!Objects.equals(email, ccdParty.getRepresentativeOrganisationEmail())) {
                    failWithMessage("Expected Representative.organisationEmail to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationEmail(), contactDetails.getEmail());
                }
            });

            contactDetails.getPhone().ifPresent(phoneNumber -> {
                if (!Objects.equals(phoneNumber, ccdParty.getRepresentativeOrganisationPhone())) {
                    failWithMessage("Expected Representative.organisationPhone to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationPhone(), contactDetails.getPhone());
                }
            });
        });

    }
}
