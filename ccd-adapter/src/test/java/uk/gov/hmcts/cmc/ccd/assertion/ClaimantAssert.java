package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
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

    public ClaimantAssert isEqualTo(CCDApplicant applicant) {
        isNotNull();

        if (this.actual instanceof Individual) {
            if (!Objects.equals(CCDPartyType.INDIVIDUAL, applicant.getPartyDetail().getType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    applicant.getPartyDetail().getType(), CCDPartyType.INDIVIDUAL);
            }

            assertIndividual(applicant);
        }

        if (actual instanceof Organisation) {
            if (!Objects.equals(CCDPartyType.ORGANISATION, applicant.getPartyDetail().getType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    applicant.getPartyDetail().getType(), CCDPartyType.ORGANISATION);
            }

            assertOrganisation(applicant);
        }

        if (actual instanceof Company) {
            if (!Objects.equals(CCDPartyType.COMPANY, applicant.getPartyDetail().getType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    applicant.getPartyDetail().getType(), CCDPartyType.COMPANY);
            }

            assertCompany(applicant);
        }

        if (actual instanceof SoleTrader) {
            if (!Objects.equals(CCDPartyType.SOLE_TRADER, applicant.getPartyDetail().getType())) {
                failWithMessage("Expected CCDClaimant.type to be <%s> but was <%s>",
                    applicant.getPartyDetail().getType(), CCDPartyType.SOLE_TRADER);
            }
            assertSoleTrader(applicant);

        }

        return this;
    }

    private void assertSoleTrader(CCDApplicant applicant) {
        SoleTrader actual = (SoleTrader) this.actual;
        assertThat(actual.getAddress()).isEqualTo(applicant.getPartyDetail().getPrimaryAddress());

        actual.getTitle().ifPresent(title -> assertThat(applicant.getPartyDetail().getTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getName(), applicant.getPartyName())) {
            failWithMessage("Expected CCDSoleTrader.name to be <%s> but was <%s>",
                applicant.getPartyName(), this.actual.getName());
        }

        String phone = actual.getPhone().orElse(null);
        if (!Objects.equals(phone, applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber())) {
            failWithMessage("Expected CCDCompany.phone to be <%s> but was <%s>",
                applicant.getPartyDetail().getTelephoneNumber().getTelephoneNumber(), phone);
        }

        if (!Objects.equals(actual.getBusinessName().orElse(null),
            applicant.getPartyDetail().getBusinessName())) {
            failWithMessage("Expected CCDSoleTrader.businessName to be <%s> but was <%s>",
                applicant.getPartyDetail().getBusinessName(), actual.getBusinessName().orElse(null));
        }

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(applicant.getPartyDetail().getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, applicant));
    }

    private void assertCompany(CCDApplicant ccdParty) {
        Company actual = (Company) this.actual;

        assertThat(actual.getAddress()).isEqualTo(ccdParty.getPartyDetail().getPrimaryAddress());
        if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
            failWithMessage("Expected CCDCompany.name to be <%s> but was <%s>",
                ccdParty.getPartyName(), actual.getName());
        }

        String phone = actual.getPhone().orElse(null);
        if (!Objects.equals(phone, ccdParty.getPartyDetail().getTelephoneNumber().getTelephoneNumber())) {
            failWithMessage("Expected CCDCompany.phone to be <%s> but was <%s>",
                ccdParty.getPartyDetail().getTelephoneNumber().getTelephoneNumber(), phone);
        }

        if (!Objects.equals(actual.getContactPerson().orElse(null),
            ccdParty.getPartyDetail().getContactPerson())) {
            failWithMessage("Expected CCDCompany.contactPerson to be <%s> but was <%s>",
                ccdParty.getPartyDetail().getContactPerson(), actual.getContactPerson().orElse(null));
        }

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdParty.getPartyDetail().getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertOrganisation(CCDApplicant ccdParty) {
        Organisation actual = (Organisation) this.actual;

        assertThat((actual).getAddress()).isEqualTo(ccdParty.getPartyDetail().getPrimaryAddress());
        if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
            failWithMessage("Expected CCDOrganisation.name to be <%s> but was <%s>",
                ccdParty.getPartyName(), actual.getName());
        }

        String phone = actual.getPhone().orElse(null);
        if (!Objects.equals(phone, ccdParty.getPartyDetail().getTelephoneNumber().getTelephoneNumber())) {
            failWithMessage("Expected CCDOrganisation.phone to be <%s> but was <%s>",
                ccdParty.getPartyDetail().getTelephoneNumber().getTelephoneNumber(), phone);
        }

        String contactPerson = actual.getContactPerson().orElse(null);
        if (!Objects.equals(contactPerson, ccdParty.getPartyDetail().getContactPerson())) {
            failWithMessage("Expected CCDOrganisation.contactPerson to be <%s> but was <%s>",
                ccdParty.getPartyDetail().getContactPerson(), contactPerson);
        }

        String companyHouseNumber = actual.getCompaniesHouseNumber().orElse(null);

        if (!Objects.equals(companyHouseNumber, ccdParty.getPartyDetail().getCompaniesHouseNumber())) {
            failWithMessage("Expected CCDOrganisation.companyHouseNumber to be <%s> but was <%s>",
                ccdParty.getPartyDetail().getCompaniesHouseNumber(), companyHouseNumber);
        }

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdParty.getPartyDetail().getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertIndividual(CCDApplicant ccdParty) {
        Individual actual = (Individual) this.actual;

        if (!Objects.equals(actual.getName(), ccdParty.getPartyName())) {
            failWithMessage("Expected CCDIndividual.name to be <%s> but was <%s>",
                ccdParty.getPartyName(), actual.getName());
        }

        if (actual.getDateOfBirth() != null
            && !Objects.equals(actual.getDateOfBirth(), ccdParty.getPartyDetail().getDateOfBirth())) {
            failWithMessage("Expected CCDIndividual.dateOfBirth to be <%s> but was <%s>",
                ccdParty.getPartyDetail().getDateOfBirth(), actual.getDateOfBirth());

        }
        assertThat((actual).getAddress()).isEqualTo(ccdParty.getPartyDetail().getPrimaryAddress());

        actual.getCorrespondenceAddress().ifPresent(address ->
            assertThat(ccdParty.getPartyDetail().getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertRepresentativeDetails(Representative representative, CCDApplicant ccdParty) {
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
