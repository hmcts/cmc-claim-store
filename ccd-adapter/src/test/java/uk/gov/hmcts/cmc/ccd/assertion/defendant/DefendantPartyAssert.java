package uk.gov.hmcts.cmc.ccd.assertion.defendant;

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
                failWithMessage("Expected CCDDefendant.partyType to be <%s> but was <%s>",
                    ccdDefendant.getPartyType(), CCDPartyType.SOLE_TRADER);
            }
            assertSoleTrader(ccdDefendant);

        }

        return this;
    }

    private void assertParty(CCDDefendant ccdDefendant) {
        if (!Objects.equals(actual.getName(), ccdDefendant.getPartyName())) {
            failWithMessage("Expected CCDDefendant.partyName to be <%s> but was <%s>",
                ccdDefendant.getPartyName(), actual.getName());
        }
        assertThat(actual.getAddress()).isEqualTo(ccdDefendant.getPartyAddress());
        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdDefendant.getPartyCorrespondenceAddress()).isEqualTo(address)
        );

        String mobilePhone = actual.getMobilePhone().orElse(null);
        if (!Objects.equals(mobilePhone, ccdDefendant.getPartyPhone())) {
            failWithMessage("Expected CCDDefendant.partyPhone to be <%s> but was <%s>",
                ccdDefendant.getPartyPhone(), mobilePhone);
        }

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdDefendant));
    }

    private void assertSoleTrader(CCDDefendant ccdDefendant) {
        assertParty(ccdDefendant);

        SoleTrader actual = (SoleTrader) this.actual;
        actual.getTitle().ifPresent(title -> assertThat(ccdDefendant.getPartyTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getBusinessName().orElse(null), ccdDefendant.getPartyBusinessName())) {
            failWithMessage("Expected CCDDefendant.partyBusinessName to be <%s> but was <%s>",
                ccdDefendant.getPartyBusinessName(), actual.getBusinessName().orElse(null));
        }
    }

    private void assertCompany(CCDDefendant ccdDefendant) {
        assertParty(ccdDefendant);
        Company actual = (Company) this.actual;

        if (!Objects.equals(actual.getContactPerson().orElse(null), ccdDefendant.getPartyContactPerson())) {
            failWithMessage("Expected CCDDefendant.partyContactPerson to be <%s> but was <%s>",
                ccdDefendant.getPartyContactPerson(), actual.getContactPerson().orElse(null));
        }
    }

    private void assertOrganisation(CCDDefendant ccdDefendant) {
        assertParty(ccdDefendant);
        Organisation actual = (Organisation) this.actual;

        String contactPerson = actual.getContactPerson().orElse(null);
        if (!Objects.equals(contactPerson, ccdDefendant.getPartyContactPerson())) {
            failWithMessage("Expected CCDDefendant.partyContactPerson to be <%s> but was <%s>",
                ccdDefendant.getPartyContactPerson(), contactPerson);
        }

        String companyHouseNumber = actual.getCompaniesHouseNumber().orElse(null);
        if (!Objects.equals(companyHouseNumber, ccdDefendant.getPartyCompaniesHouseNumber())) {
            failWithMessage("Expected CCDDefendant.partyCompaniesHouseNumber to be <%s> but was <%s>",
                ccdDefendant.getPartyCompaniesHouseNumber(), companyHouseNumber);
        }
    }

    private void assertIndividual(CCDDefendant ccdDefendant) {
        assertParty(ccdDefendant);
        Individual actual = (Individual) this.actual;

        if (actual.getDateOfBirth() != null
            && !Objects.equals(actual.getDateOfBirth(), ccdDefendant.getPartyDateOfBirth())) {
            failWithMessage("Expected CCDDefendant.partyDateOfBirth to be <%s> but was <%s>",
                ccdDefendant.getPartyDateOfBirth(), actual.getDateOfBirth());
        }
    }

    private void assertRepresentativeDetails(Representative representative, CCDDefendant ccdParty) {
        if (!Objects.equals(representative.getOrganisationName(), ccdParty.getRepresentativeOrganisationName())) {
            failWithMessage("Expected CCDDefendant.representativeOrganisationName to be <%s> but was <%s>",
                ccdParty.getRepresentativeOrganisationName(), representative.getOrganisationName());
        }

        assertThat(representative.getOrganisationAddress()).isEqualTo(ccdParty.getRepresentativeOrganisationAddress());

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
