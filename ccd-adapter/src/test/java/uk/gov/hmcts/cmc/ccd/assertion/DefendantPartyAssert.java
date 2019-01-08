package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;

public class DefendantPartyAssert extends AbstractAssert<DefendantPartyAssert, Party> {

    public DefendantPartyAssert(Party party) {
        super(party, DefendantPartyAssert.class);
    }

    public DefendantPartyAssert isEqualTo(CCDDefendant ccdDefendant) {
        isNotNull();

        if (this.actual instanceof Individual) {
            if (!Objects.equals(INDIVIDUAL, ccdDefendant.getPartyType())) {
                failWithMessage("Expected CCDDefendant.partyType to be <%s> but was <%s>",
                    ccdDefendant.getPartyType(), INDIVIDUAL);
            }

            assertIndividual(ccdDefendant);
        }

        if (actual instanceof Organisation) {
            if (!Objects.equals(ORGANISATION, ccdDefendant.getPartyType())) {
                failWithMessage("Expected CCDDefendant.partyType to be <%s> but was <%s>",
                    ccdDefendant.getPartyType(), ORGANISATION);
            }

            assertOrganisation(ccdDefendant);
        }

        if (actual instanceof Company) {
            if (!Objects.equals(COMPANY, ccdDefendant.getPartyType())) {
                failWithMessage("Expected CCDDefendant.partyType to be <%s> but was <%s>",
                    ccdDefendant.getPartyType(), COMPANY);
            }

            assertCompany(ccdDefendant);
        }

        if (actual instanceof SoleTrader) {
            if (!Objects.equals(CCDPartyType.SOLE_TRADER, ccdDefendant.getPartyType())) {
                failWithMessage("Expected CCDDefendant.type to be <%s> but was <%s>",
                    ccdDefendant.getPartyType(), CCDPartyType.SOLE_TRADER);
            }
            assertSoleTrader(ccdDefendant);

        }

        return this;
    }

    private void assertSoleTrader(CCDDefendant ccdParty) {
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

    private void assertCompany(CCDDefendant ccdParty) {
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

    private void assertOrganisation(CCDDefendant ccdParty) {
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

    private void assertIndividual(CCDDefendant ccdParty) {
        Individual actual = (Individual) this.actual;

        if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
            failWithMessage("Expected CCDIndividual.partyName to be <%s> but was <%s>",
                ccdParty.getPartyName(), actual.getName());
        }

        if (actual.getDateOfBirth() != null
            && !Objects.equals(actual.getDateOfBirth(), ccdParty.getPartyDateOfBirth())) {
            failWithMessage("Expected CCDIndividual.partyDateOfBirth to be <%s> but was <%s>",
                ccdParty.getPartyDateOfBirth(), actual.getDateOfBirth());

        }
        assertThat((actual).getAddress()).isEqualTo(ccdParty.getPartyAddress());

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdParty.getPartyCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertRepresentativeDetails(Representative representative, CCDDefendant ccdParty) {
        if (!Objects.equals(representative.getOrganisationName(), ccdParty.getRepresentativeOrganisationName())) {
            failWithMessage("Expected CCDDefendant.representativeOrganisationName to be <%s> but was <%s>",
                ccdParty.getRepresentativeOrganisationName(), representative.getOrganisationName());
        }

        assertThat(representative.getOrganisationAddress())
            .isEqualTo(ccdParty.getRepresentativeOrganisationAddress());

        representative.getOrganisationContactDetails().ifPresent(contactDetails -> {

            contactDetails.getDxAddress().ifPresent(dxAddress -> {
                if (!Objects.equals(dxAddress, ccdParty.getRepresentativeOrganisationDxAddress())) {
                    failWithMessage(
                        "Expected CCDDefendant.representativeOrganisationDxAddress to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationDxAddress(), contactDetails.getDxAddress());
                }
            });

            contactDetails.getEmail().ifPresent(email -> {
                if (!Objects.equals(email, ccdParty.getRepresentativeOrganisationEmail())) {
                    failWithMessage(
                        "Expected CCDDefendant.representativeOrganisationEmail to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationEmail(), contactDetails.getEmail());
                }
            });

            contactDetails.getPhone().ifPresent(phoneNumber -> {
                if (!Objects.equals(phoneNumber, ccdParty.getRepresentativeOrganisationPhone())) {
                    failWithMessage(
                        "Expected CCDDefendant.representativeOrganisationPhone to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationPhone(), contactDetails.getPhone());
                }
            });
        });

    }
}
