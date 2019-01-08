package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDDefendant;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;

public class TheirDetailsAssert extends AbstractAssert<TheirDetailsAssert, TheirDetails> {

    public TheirDetailsAssert(TheirDetails party) {
        super(party, TheirDetailsAssert.class);
    }

    public TheirDetailsAssert isEqualTo(CCDDefendant ccdParty) {
        isNotNull();

        if (actual instanceof IndividualDetails) {
            if (!Objects.equals(INDIVIDUAL, ccdParty.getClaimantProvidedType())) {
                failWithMessage("Expected CCDDefendant.claimantProvidedType to be <%s> but was <%s>",
                    ccdParty.getClaimantProvidedType(), INDIVIDUAL);
            }

            assertIndividualDetails(ccdParty);
        }

        if (actual instanceof OrganisationDetails) {
            if (!Objects.equals(ORGANISATION, ccdParty.getClaimantProvidedType())) {
                failWithMessage("Expected CCDDefendant.claimantProvidedType to be <%s> but was <%s>",
                    ccdParty.getClaimantProvidedType(), ORGANISATION);
            }

            assertOrganisationDetails(ccdParty);
        }

        if (actual instanceof CompanyDetails) {
            if (!Objects.equals(COMPANY, ccdParty.getClaimantProvidedType())) {
                failWithMessage("Expected CCDDefendant.claimantProvidedType to be <%s> but was <%s>",
                    ccdParty.getClaimantProvidedType(), COMPANY);
            }

            assertCompanyDetails(ccdParty);
        }

        if (actual instanceof SoleTraderDetails) {
            if (!Objects.equals(SOLE_TRADER, ccdParty.getClaimantProvidedType())) {
                failWithMessage("Expected CCDDefendant.claimantProvidedType to be <%s> but was <%s>",
                    ccdParty.getClaimantProvidedType(), SOLE_TRADER);
            }

            assertSoleTraderDetails(ccdParty);
        }

        return this;
    }

    private void assertSoleTraderDetails(CCDDefendant ccdParty) {
        SoleTraderDetails actual = (SoleTraderDetails) this.actual;
        assertThat(actual.getAddress()).isEqualTo(ccdParty.getClaimantProvidedAddress());

        actual.getTitle().ifPresent(title -> assertThat(ccdParty.getClaimantProvidedTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getName(), ccdParty.getClaimantProvidedName())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedName to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdParty.getClaimantProvidedEmail())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedEmail to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedEmail(), this.actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getBusinessName().orElse(null),
            ccdParty.getClaimantProvidedBusinessName())
        ) {
            failWithMessage("Expected CCDDefendant.claimantProvideBusinessName to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedBusinessName(), actual.getBusinessName().orElse(null));
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(ccdParty.getClaimantProvidedServiceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertCompanyDetails(CCDDefendant ccdParty) {
        CompanyDetails actual = (CompanyDetails) this.actual;

        assertThat(actual.getAddress()).isEqualTo(ccdParty.getClaimantProvidedAddress());
        if (!Objects.equals(actual.getName(), ccdParty.getClaimantProvidedName())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedName to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdParty.getClaimantProvidedEmail())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedEmail to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedEmail(), actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getContactPerson().orElse(null),
            ccdParty.getClaimantProvidedContactPerson())
        ) {
            failWithMessage("Expected CCDDefendant.claimantProvidedContactPerson to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedContactPerson(), actual.getContactPerson().orElse(null));
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(ccdParty.getClaimantProvidedServiceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertOrganisationDetails(CCDDefendant ccdParty) {
        OrganisationDetails actual = (OrganisationDetails) this.actual;

        assertThat(actual.getAddress()).isEqualTo(ccdParty.getClaimantProvidedAddress());
        if (!Objects.equals(actual.getName(), ccdParty.getClaimantProvidedName())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedName to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdParty.getClaimantProvidedEmail())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedEmail to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedEmail(), actual.getEmail().orElse(null));
        }

        String contactPerson = actual.getContactPerson().orElse(null);
        if (!Objects.equals(contactPerson, ccdParty.getClaimantProvidedContactPerson())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedContactPerson to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedContactPerson(), contactPerson);
        }

        String companyHouseNumber = actual.getCompaniesHouseNumber().orElse(null);

        if (!Objects.equals(companyHouseNumber, ccdParty.getClaimantProvidedCompaniesHouseNumber())) {
            failWithMessage(
                "Expected CCDDefendant.claimantProvidedCompaniesHouseNumber to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedCompaniesHouseNumber(), companyHouseNumber);
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(ccdParty.getClaimantProvidedServiceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertIndividualDetails(CCDDefendant ccdParty) {
        IndividualDetails actual = (IndividualDetails) this.actual;

        assertThat(actual.getAddress()).isEqualTo(ccdParty.getClaimantProvidedAddress());
        if (!Objects.equals(actual.getName(), ccdParty.getClaimantProvidedName())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedName to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedName(), actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), ccdParty.getClaimantProvidedEmail())) {
            failWithMessage("Expected CCDDefendant.claimantProvidedEmail to be <%s> but was <%s>",
                ccdParty.getClaimantProvidedEmail(), actual.getEmail().orElse(null));
        }
        actual.getDateOfBirth().ifPresent(dob -> assertThat(dob).isEqualTo(ccdParty.getClaimantProvidedDateOfBirth()));

        actual.getServiceAddress().ifPresent(address ->
            assertThat(ccdParty.getClaimantProvidedServiceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, ccdParty));
    }

    private void assertRepresentativeDetails(Representative representative, CCDDefendant ccdParty) {
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
