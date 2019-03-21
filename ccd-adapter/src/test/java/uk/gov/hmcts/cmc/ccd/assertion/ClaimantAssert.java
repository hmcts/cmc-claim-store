package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimant;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ClaimantAssert extends AbstractAssert<ClaimantAssert, Party> {

    public ClaimantAssert(Party party) {
        super(party, ClaimantAssert.class);
    }

    public ClaimantAssert isEqualTo(CCDClaimant ccdParty) {
        isNotNull();

        if (this.actual instanceof Individual) {
            if (!Objects.equals(CCDPartyType.INDIVIDUAL, ccdParty.getPartyType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getPartyType(), CCDPartyType.INDIVIDUAL);
            }

            assertIndividual(ccdParty);
        }

        if (actual instanceof Organisation) {
            if (!Objects.equals(CCDPartyType.ORGANISATION, ccdParty.getPartyType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getPartyType(), CCDPartyType.ORGANISATION);
            }

            assertOrganisation(ccdParty);
        }

        if (actual instanceof Company) {
            if (!Objects.equals(CCDPartyType.COMPANY, ccdParty.getPartyType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getPartyType(), CCDPartyType.COMPANY);
            }

            assertCompany(ccdParty);
        }

        if (actual instanceof SoleTrader) {
            if (!Objects.equals(CCDPartyType.SOLE_TRADER, ccdParty.getPartyType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    ccdParty.getPartyType(), CCDPartyType.SOLE_TRADER);
            }
            assertSoleTrader(ccdParty);

        }

        return this;
    }

    private void assertSoleTrader(CCDClaimant ccdParty) {
        SoleTrader actual = (SoleTrader) this.actual;
        assertThat(actual.getAddress()).isEqualTo(ccdParty.getPartyAddress());

        actual.getTitle().ifPresent(title -> assertThat(ccdParty.getPartyTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
            failWithMessage("Expected CCDSoleTrader.name to be <%s> but was <%s>",
                ccdParty.getPartyName(), this.actual.getName());
        }

        String mobilePhone = actual.getMobilePhone().orElse(null);
        if (!Objects.equals(mobilePhone, ccdParty.getPartyPhone())) {
            failWithMessage("Expected CCDCompany.mobilePhone to be <%s> but was <%s>",
                ccdParty.getPartyPhone(), mobilePhone);
        }

        if (!Objects.equals(actual.getBusinessName().orElse(null), ccdParty.getPartyBusinessName())) {
            failWithMessage("Expected CCDSoleTrader.businessName to be <%s> but was <%s>",
                ccdParty.getPartyBusinessName(), actual.getBusinessName().orElse(null));
        }

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdParty.getPartyCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertCompany(CCDClaimant ccdParty) {
        Company actual = (Company) this.actual;

        assertThat(actual.getAddress()).isEqualTo(ccdParty.getPartyAddress());
        if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
            failWithMessage("Expected CCDCompany.name to be <%s> but was <%s>",
                ccdParty.getPartyName(), actual.getName());
        }

        String mobilePhone = actual.getMobilePhone().orElse(null);
        if (!Objects.equals(mobilePhone, ccdParty.getPartyPhone())) {
            failWithMessage("Expected CCDCompany.mobilePhone to be <%s> but was <%s>",
                ccdParty.getPartyPhone(), mobilePhone);
        }

        if (!Objects.equals(actual.getContactPerson().orElse(null), ccdParty.getPartyContactPerson())) {
            failWithMessage("Expected CCDCompany.contactPerson to be <%s> but was <%s>",
                ccdParty.getPartyContactPerson(), actual.getContactPerson().orElse(null));
        }

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdParty.getPartyCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertOrganisation(CCDClaimant ccdParty) {
        Organisation actual = (Organisation) this.actual;

        assertThat((actual).getAddress()).isEqualTo(ccdParty.getPartyAddress());
        if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
            failWithMessage("Expected CCDOrganisation.name to be <%s> but was <%s>",
                ccdParty.getPartyName(), actual.getName());
        }

        String mobilePhone = actual.getMobilePhone().orElse(null);
        if (!Objects.equals(mobilePhone, ccdParty.getPartyPhone())) {
            failWithMessage("Expected CCDOrganisation.mobilePhone to be <%s> but was <%s>",
                ccdParty.getPartyPhone(), mobilePhone);
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

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdParty.getPartyCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertIndividual(CCDClaimant ccdParty) {
        Individual actual = (Individual) this.actual;

        if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
            failWithMessage("Expected CCDIndividual.name to be <%s> but was <%s>",
                ccdParty.getPartyName(), actual.getName());
        }

        if (actual.getDateOfBirth() != null
            && !Objects.equals(actual.getDateOfBirth(), ccdParty.getPartyDateOfBirth())) {
            failWithMessage("Expected CCDIndividual.dateOfBirth to be <%s> but was <%s>",
                ccdParty.getPartyDateOfBirth(), actual.getDateOfBirth());

        }
        assertThat((actual).getAddress()).isEqualTo(ccdParty.getPartyAddress());

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdParty.getPartyCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertRepresentativeDetails(Representative representative, CCDClaimant ccdParty) {
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
