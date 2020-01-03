package uk.gov.hmcts.cmc.ccd.assertion;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class ClaimantAssert extends CustomAssert<ClaimantAssert, Party> {

    ClaimantAssert(Party party) {
        super("Claimant", party, ClaimantAssert.class);
    }

    public ClaimantAssert isEqualTo(CCDApplicant expected) {
        isNotNull();

        if (this.actual instanceof Individual) {
            if (!Objects.equals(CCDPartyType.INDIVIDUAL, expected.getPartyDetail().getType())) {
                failExpectedEqual("type", expected.getPartyDetail().getType(), CCDPartyType.INDIVIDUAL);
            }
            assertIndividual(expected);
        }

        if (actual instanceof Organisation) {
            if (!Objects.equals(CCDPartyType.ORGANISATION, expected.getPartyDetail().getType())) {
                failExpectedEqual("type", expected.getPartyDetail().getType(), CCDPartyType.ORGANISATION);
            }
            assertOrganisation(expected);
        }

        if (actual instanceof Company) {
            if (!Objects.equals(CCDPartyType.COMPANY, expected.getPartyDetail().getType())) {
                failExpectedEqual("type", expected.getPartyDetail().getType(), CCDPartyType.COMPANY);
            }
            assertCompany(expected);
        }

        if (actual instanceof SoleTrader) {
            if (!Objects.equals(CCDPartyType.SOLE_TRADER, expected.getPartyDetail().getType())) {
                failExpectedEqual("type", expected.getPartyDetail().getType(), CCDPartyType.SOLE_TRADER);
            }
            assertSoleTrader(expected);
        }

        return this;
    }

    private void assertSoleTrader(CCDApplicant expected) {
        SoleTrader soleTrader = (SoleTrader) this.actual;

        compare("address",
            expected.getPartyDetail().getPrimaryAddress(),
            Optional.ofNullable(soleTrader.getAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("title",
            expected.getPartyDetail().getTitle(),
            soleTrader.getTitle());

        compare("name",
            expected.getPartyName(),
            Optional.ofNullable(soleTrader.getName()));

        compare("phone",
            expected.getPartyDetail().getTelephoneNumber(), CCDTelephone::getTelephoneNumber,
            soleTrader.getPhone());

        compare("businessName",
            expected.getPartyDetail().getBusinessName(),
            soleTrader.getBusinessName());

        compare("correspondenceAddress",
            expected.getPartyDetail().getCorrespondenceAddress(),
            soleTrader.getCorrespondenceAddress(),
            (e, a) -> assertThat(a).isEqualTo(e));

        assertRepresentativeDetails(expected);
    }

    private void assertCompany(CCDApplicant expected) {
        Company company = (Company) this.actual;

        compare("address",
            expected.getPartyDetail().getPrimaryAddress(),
            Optional.ofNullable(company.getAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("name",
            expected.getPartyName(),
            Optional.ofNullable(company.getName()));

        compare("phone",
            expected.getPartyDetail().getTelephoneNumber(), CCDTelephone::getTelephoneNumber,
            company.getPhone());

        compare("contactPerson",
            expected.getPartyDetail().getContactPerson(),
            company.getContactPerson());

        compare("correspondenceAddress",
            expected.getPartyDetail().getCorrespondenceAddress(),
            company.getCorrespondenceAddress(),
            (e, a) -> assertThat(a).isEqualTo(e));

        assertRepresentativeDetails(expected);
    }

    private void assertOrganisation(CCDApplicant expected) {
        Organisation organisation = (Organisation) this.actual;

        compare("address",
            expected.getPartyDetail().getPrimaryAddress(),
            Optional.ofNullable(organisation.getAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("name",
            expected.getPartyName(),
            Optional.ofNullable(organisation.getName()));

        compare("phone",
            expected.getPartyDetail().getTelephoneNumber(), CCDTelephone::getTelephoneNumber,
            organisation.getPhone());

        compare("contactPerson",
            expected.getPartyDetail().getContactPerson(),
            organisation.getContactPerson());

        compare("companyHouseNumber",
            expected.getPartyDetail().getCompaniesHouseNumber(),
            organisation.getCompaniesHouseNumber());

        compare("correspondenceAddress",
            expected.getPartyDetail().getCorrespondenceAddress(),
            organisation.getCorrespondenceAddress(),
            (e, a) -> assertThat(a).isEqualTo(e));

        assertRepresentativeDetails(expected);
    }

    private void assertIndividual(CCDApplicant expected) {
        Individual individual = (Individual) this.actual;

        compare("name",
            expected.getPartyName(),
            Optional.ofNullable(individual.getName()));

        compare("dateOfBirth",
            expected.getPartyDetail().getDateOfBirth(),
            Optional.ofNullable(individual.getDateOfBirth()));

        compare("address",
            expected.getPartyDetail().getPrimaryAddress(),
            Optional.ofNullable(individual.getAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("correspondenceAddress",
            expected.getPartyDetail().getCorrespondenceAddress(),
            individual.getCorrespondenceAddress(),
            (e, a) -> assertThat(a).isEqualTo(e));

        assertRepresentativeDetails(expected);
    }

    private void assertRepresentativeDetails(CCDApplicant expected) {
        Representative representative = actual.getRepresentative().orElse(null);

        if (representative == null) {
            if (expected.hasRepresentative()) {
                failExpectedPresent("representative", ImmutableMap.of(
                    "organisationName", expected.getRepresentativeOrganisationName(),
                    "organisationPhone", expected.getRepresentativeOrganisationPhone(),
                    "organisationAddress", expected.getRepresentativeOrganisationAddress(),
                    "organisationEmail", expected.getRepresentativeOrganisationEmail(),
                    "organisationDxAddress", expected.getRepresentativeOrganisationDxAddress()
                ));
            }
            return;
        }

        compare("organisationName",
            expected.getRepresentativeOrganisationName(),
            Optional.ofNullable(representative.getOrganisationName()));

        compare("organisationAddress",
            expected.getRepresentativeOrganisationAddress(),
            Optional.ofNullable(representative.getOrganisationAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("organisationDxAddress",
            expected.getRepresentativeOrganisationDxAddress(),
            representative.getOrganisationContactDetails().flatMap(ContactDetails::getDxAddress));

        compare("organisationEmail",
            expected.getRepresentativeOrganisationEmail(),
            representative.getOrganisationContactDetails().flatMap(ContactDetails::getEmail));

        compare("organisationPhone",
            expected.getRepresentativeOrganisationPhone(),
            representative.getOrganisationContactDetails().flatMap(ContactDetails::getPhone));
    }
}
