package uk.gov.hmcts.cmc.ccd.assertion.defendant;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
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

    public DefendantPartyAssert isEqualTo(CCDRespondent ccdRespondent) {
        isNotNull();

        if (this.actual instanceof Individual) {
            if (!Objects.equals(INDIVIDUAL, ccdRespondent.getPartyDetail().getType())) {
                failWithMessage("Expected CCDRespondent.partyType to be <%s> but was <%s>",
                    ccdRespondent.getPartyDetail().getType(), INDIVIDUAL);
            }

            assertIndividual(ccdRespondent);
        }

        if (actual instanceof Organisation) {
            if (!Objects.equals(ORGANISATION, ccdRespondent.getPartyDetail().getType())) {
                failWithMessage("Expected CCDRespondent.partyType to be <%s> but was <%s>",
                    ccdRespondent.getPartyDetail().getType(), ORGANISATION);
            }

            assertOrganisation(ccdRespondent);
        }

        if (actual instanceof Company) {
            if (!Objects.equals(COMPANY, ccdRespondent.getPartyDetail().getType())) {
                failWithMessage("Expected CCDRespondent.partyType to be <%s> but was <%s>",
                    ccdRespondent.getPartyDetail().getType(), COMPANY);
            }

            assertCompany(ccdRespondent);
        }

        if (actual instanceof SoleTrader) {
            if (!Objects.equals(CCDPartyType.SOLE_TRADER, ccdRespondent.getPartyDetail().getType())) {
                failWithMessage("Expected CCDRespondent.partyType to be <%s> but was <%s>",
                    ccdRespondent.getPartyDetail().getType(), CCDPartyType.SOLE_TRADER);
            }
            assertSoleTrader(ccdRespondent);

        }

        return this;
    }

    private void assertParty(CCDRespondent ccdRespondent) {
        CCDParty partyDetail = ccdRespondent.getPartyDetail();
        if (!Objects.equals(actual.getName(), ccdRespondent.getPartyName())) {
            failWithMessage("Expected CCDRespondent.partyName to be <%s> but was <%s>",
                ccdRespondent.getPartyName(), actual.getName());
        }
        assertThat(actual.getAddress()).isEqualTo(partyDetail.getPrimaryAddress());
        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(partyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        String phone = actual.getPhone().orElse(null);
        if (!Objects.equals(phone, partyDetail.getTelephoneNumber().getTelephoneNumber())) {
            failWithMessage("Expected CCDRespondent.partyPhone to be <%s> but was <%s>",
                partyDetail.getTelephoneNumber().getTelephoneNumber(), phone);
        }

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdRespondent));
    }

    private void assertSoleTrader(CCDRespondent ccdRespondent) {
        assertParty(ccdRespondent);

        SoleTrader actual = (SoleTrader) this.actual;
        actual.getTitle().ifPresent(title -> assertThat(ccdRespondent.getPartyDetail().getTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getBusinessName().orElse(null),
            ccdRespondent.getPartyDetail().getBusinessName())) {
            failWithMessage("Expected CCDRespondent.partyBusinessName to be <%s> but was <%s>",
                ccdRespondent.getPartyDetail().getBusinessName(), actual.getBusinessName().orElse(null));
        }
    }

    private void assertCompany(CCDRespondent ccdRespondent) {
        assertParty(ccdRespondent);
        Company actual = (Company) this.actual;

        if (!Objects.equals(actual.getContactPerson().orElse(null),
            ccdRespondent.getPartyDetail().getContactPerson())) {
            failWithMessage("Expected CCDRespondent.partyContactPerson to be <%s> but was <%s>",
                ccdRespondent.getPartyDetail().getContactPerson(), actual.getContactPerson().orElse(null));
        }
    }

    private void assertOrganisation(CCDRespondent ccdRespondent) {
        assertParty(ccdRespondent);
        Organisation actual = (Organisation) this.actual;

        String contactPerson = actual.getContactPerson().orElse(null);
        if (!Objects.equals(contactPerson, ccdRespondent.getPartyDetail().getContactPerson())) {
            failWithMessage("Expected CCDRespondent.partyContactPerson to be <%s> but was <%s>",
                ccdRespondent.getPartyDetail().getContactPerson(), contactPerson);
        }

        String companyHouseNumber = actual.getCompaniesHouseNumber().orElse(null);
        if (!Objects.equals(companyHouseNumber, ccdRespondent.getPartyDetail().getCompaniesHouseNumber())) {
            failWithMessage("Expected CCDRespondent.partyCompaniesHouseNumber to be <%s> but was <%s>",
                ccdRespondent.getPartyDetail().getCompaniesHouseNumber(), companyHouseNumber);
        }
    }

    private void assertIndividual(CCDRespondent ccdRespondent) {
        assertParty(ccdRespondent);
        Individual actual = (Individual) this.actual;

        if (actual.getDateOfBirth() != null
            && !Objects.equals(actual.getDateOfBirth(), ccdRespondent.getPartyDetail().getDateOfBirth())) {
            failWithMessage("Expected CCDRespondent.partyDateOfBirth to be <%s> but was <%s>",
                ccdRespondent.getPartyDetail().getDateOfBirth(), actual.getDateOfBirth());
        }
    }

    private void assertRepresentativeDetails(Representative representative, CCDRespondent ccdParty) {
        if (!Objects.equals(representative.getOrganisationName(), ccdParty.getRepresentativeOrganisationName())) {
            failWithMessage("Expected CCDRespondent.representativeOrganisationName "
                    + "to be <%s> but was <%s>",
                ccdParty.getRepresentativeOrganisationName(), representative.getOrganisationName());
        }

        assertThat(representative.getOrganisationAddress()).isEqualTo(ccdParty.getRepresentativeOrganisationAddress());

        representative.getOrganisationContactDetails().ifPresent(contactDetails -> {
            contactDetails.getDxAddress().ifPresent(dxAddress -> {
                if (!Objects.equals(dxAddress, ccdParty.getRepresentativeOrganisationDxAddress())) {
                    failWithMessage("Expected CCDRespondent.representativeOrganisationDxAddress "
                            + "to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationDxAddress(), contactDetails.getDxAddress());
                }
            });

            contactDetails.getEmail().ifPresent(email -> {
                if (!Objects.equals(email, ccdParty.getRepresentativeOrganisationEmail())) {
                    failWithMessage(
                        "Expected CCDRespondent.representativeOrganisationEmail to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationEmail(), contactDetails.getEmail());
                }
            });

            contactDetails.getPhone().ifPresent(phoneNumber -> {
                if (!Objects.equals(phoneNumber, ccdParty.getRepresentativeOrganisationPhone())) {
                    failWithMessage(
                        "Expected CCDRespondent.representativeOrganisationPhone to be <%s> but was <%s>",
                        ccdParty.getRepresentativeOrganisationPhone(), contactDetails.getPhone());
                }
            });
        });
    }
}
