package uk.gov.hmcts.cmc.ccd.assertion.defendant;

import uk.gov.hmcts.cmc.ccd.assertion.CustomAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CCDTelephone;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
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
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;

public class DefendantPartyAssert extends CustomAssert<DefendantPartyAssert, Party> {

    public DefendantPartyAssert(Party party) {
        super("CCDRespondent", party, DefendantPartyAssert.class);
    }

    public DefendantPartyAssert isEqualTo(CCDRespondent expected) {
        isNotNull();

        if (actual instanceof Individual) {
            if (!Objects.equals(INDIVIDUAL, expected.getPartyDetail().getType())) {
                failExpectedEqual("partyType", expected.getPartyDetail().getType(), INDIVIDUAL);
            }

            assertIndividual(expected);
        }

        if (actual instanceof Organisation) {
            if (!Objects.equals(ORGANISATION, expected.getPartyDetail().getType())) {
                failExpectedEqual("partyType", expected.getPartyDetail().getType(), ORGANISATION);
            }

            assertOrganisation(expected);
        }

        if (actual instanceof Company) {
            if (!Objects.equals(COMPANY, expected.getPartyDetail().getType())) {
                failExpectedEqual("partyType", expected.getPartyDetail().getType(), COMPANY);
            }

            assertCompany(expected);
        }

        if (actual instanceof SoleTrader) {
            if (!Objects.equals(CCDPartyType.SOLE_TRADER, expected.getPartyDetail().getType())) {
                failExpectedEqual("partyType", expected.getPartyDetail().getType(), SOLE_TRADER);
            }
            assertSoleTrader(expected);

        }

        return this;
    }

    private void assertParty(CCDRespondent expected) {
        compare("partyName",
            expected.getPartyName(),
            Optional.ofNullable(actual.getName()));

        CCDParty partyDetail = expected.getPartyDetail();

        compare("address",
            partyDetail, CCDParty::getPrimaryAddress,
            Optional.ofNullable(actual.getAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("correspondenceAddress",
            partyDetail, CCDParty::getCorrespondenceAddress,
            actual.getCorrespondenceAddress(),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("phone",
            partyDetail.getTelephoneNumber(), CCDTelephone::getTelephoneNumber,
            actual.getPhone());

        assertRepresentativeDetails(actual.getRepresentative().orElse(null), expected);
    }

    private void assertSoleTrader(CCDRespondent expected) {
        assertParty(expected);

        SoleTrader actual = (SoleTrader) this.actual;
        compare("partyDetail.title",
            expected.getPartyDetail(), CCDParty::getTitle,
            actual.getTitle());

        compare("partyDetail.businessName",
            expected.getPartyDetail(), CCDParty::getBusinessName,
            actual.getBusinessName());
    }

    private void assertCompany(CCDRespondent expected) {
        assertParty(expected);
        Company actual = (Company) this.actual;

        compare("partyDetail.contactPerson",
            expected.getPartyDetail().getContactPerson(),
            actual.getContactPerson());
    }

    private void assertOrganisation(CCDRespondent expected) {
        assertParty(expected);
        Organisation actual = (Organisation) this.actual;

        compare("partyContactPerson",
            expected.getPartyDetail().getContactPerson(),
            actual.getContactPerson());

        compare("partyCompaniesHouseNumber",
            expected.getPartyDetail().getCompaniesHouseNumber(),
            actual.getCompaniesHouseNumber());
    }

    private void assertIndividual(CCDRespondent expected) {
        assertParty(expected);
        Individual actual = (Individual) this.actual;

        compare("partyDateOfBirth",
            expected.getPartyDetail(), CCDParty::getDateOfBirth,
            Optional.ofNullable(actual.getDateOfBirth()));
    }

    private void assertRepresentativeDetails(Representative actual, CCDRespondent expected) {
        if (actual == null && !expected.hasRepresentative()) {
            // absent as expected
            return;
        }

        if (actual == null) {
            failExpectedPresent("representative", expected.getRepresentativeOrganisationName());
            return;
        }

        if (!expected.hasRepresentative()) {
            failExpectedAbsent("representative", actual);
        }

        compare("representativeOrganisationName",
            expected.getRepresentativeOrganisationName(),
            Optional.ofNullable(actual.getOrganisationName()));

        compare("representativeOrganisationAddress",
            expected.getRepresentativeOrganisationAddress(),
            Optional.ofNullable(actual.getOrganisationAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("representativeOrganisationDxAddress",
            expected.getRepresentativeOrganisationDxAddress(),
            actual.getOrganisationContactDetails().flatMap(ContactDetails::getDxAddress));

        compare("representativeOrganisationEmail",
            expected.getRepresentativeOrganisationEmail(),
            actual.getOrganisationContactDetails().flatMap(ContactDetails::getEmail));

        compare("representativeOrganisationPhone",
            expected.getRepresentativeOrganisationPhone(),
            actual.getOrganisationContactDetails().flatMap(ContactDetails::getPhone));
    }
}
