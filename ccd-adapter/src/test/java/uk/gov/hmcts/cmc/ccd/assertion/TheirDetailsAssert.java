package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.CCDPartyType;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.legalrep.ContactDetails;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;

public class TheirDetailsAssert extends CustomAssert<TheirDetailsAssert, TheirDetails> {

    TheirDetailsAssert(TheirDetails party) {
        super("CCDRespondent", party, TheirDetailsAssert.class);
    }

    public TheirDetailsAssert isEqualTo(CCDRespondent expected) {
        isNotNull();

        CCDParty partyDetails = expected.getClaimantProvidedDetail();
        CCDPartyType actualType = partyDetails.getType();
        if (actual instanceof IndividualDetails) {
            if (!Objects.equals(INDIVIDUAL, actualType)) {
                failExpectedEqual("claimantProvidedType", actualType, INDIVIDUAL);
            }
            assertIndividualDetails(expected);
        }

        if (actual instanceof OrganisationDetails) {
            if (!Objects.equals(ORGANISATION, actualType)) {
                failExpectedEqual("claimantProvidedType", actualType, ORGANISATION);
            }
            assertOrganisationDetails(expected);
        }

        if (actual instanceof CompanyDetails) {
            if (!Objects.equals(COMPANY, actualType)) {
                failExpectedEqual("claimantProvidedType", actualType, COMPANY);
            }
            assertCompanyDetails(expected);
        }

        if (actual instanceof SoleTraderDetails) {
            if (!Objects.equals(SOLE_TRADER, actualType)) {
                failExpectedEqual("claimantProvidedType", actualType, SOLE_TRADER);
            }
            assertSoleTraderDetails(expected);
        }

        return this;
    }

    private void assertCommonDetails(CCDRespondent expected) {
        CCDParty expectedDetail = expected.getClaimantProvidedDetail();

        compare("primaryAddress",
            expectedDetail.getPrimaryAddress(),
            Optional.ofNullable(actual.getAddress()),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("claimantProvidedName",
            expected.getClaimantProvidedPartyName(),
            Optional.ofNullable(actual.getName()));

        compare("claimantProvidedEmail",
            expectedDetail.getEmailAddress(),
            actual.getEmail());

        compare("correspondenceAddress",
            expectedDetail.getCorrespondenceAddress(),
            actual.getServiceAddress(),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("representative.organisationName",
            expected.getClaimantProvidedRepresentativeOrganisationName(),
            actual.getRepresentative().map(Representative::getOrganisationName));

        compare("representative.organisationAddress",
            expected.getClaimantProvidedRepresentativeOrganisationAddress(),
            actual.getRepresentative().map(Representative::getOrganisationAddress),
            (e, a) -> assertThat(a).isEqualTo(e));

        compare("representative.organisationDxAddress",
            expected.getClaimantProvidedRepresentativeOrganisationDxAddress(),
            actual.getRepresentative().flatMap(Representative::getOrganisationContactDetails)
                .flatMap(ContactDetails::getDxAddress));

        compare("representative.organisationEmail",
            expected.getClaimantProvidedRepresentativeOrganisationEmail(),
            actual.getRepresentative().flatMap(Representative::getOrganisationContactDetails)
                .flatMap(ContactDetails::getEmail));

        compare("representative.organisationPhone",
            expected.getClaimantProvidedRepresentativeOrganisationPhone(),
            actual.getRepresentative().flatMap(Representative::getOrganisationContactDetails)
                .flatMap(ContactDetails::getPhone));
    }

    private void assertSoleTraderDetails(CCDRespondent expected) {
        assertCommonDetails(expected);

        SoleTraderDetails soleTrader = (SoleTraderDetails) actual;
        CCDParty expectedDetail = expected.getClaimantProvidedDetail();

        compare("title",
            expectedDetail.getTitle(),
            soleTrader.getTitle());

        compare("claimantProvidedBusinessName",
            expectedDetail.getBusinessName(),
            soleTrader.getBusinessName());
    }

    private void assertCompanyDetails(CCDRespondent expected) {
        assertCommonDetails(expected);

        CompanyDetails company = (CompanyDetails) actual;
        CCDParty expectedDetail = expected.getClaimantProvidedDetail();

        compare("contactPerson",
            expectedDetail.getContactPerson(),
            company.getContactPerson());
    }

    private void assertOrganisationDetails(CCDRespondent expected) {
        assertCommonDetails(expected);

        OrganisationDetails organisation = (OrganisationDetails) actual;
        CCDParty expectedDetail = expected.getClaimantProvidedDetail();

        compare("contactPerson",
            expectedDetail.getContactPerson(),
            organisation.getContactPerson());

        compare("companiesHouseNumber",
            expectedDetail.getCompaniesHouseNumber(),
            organisation.getCompaniesHouseNumber());
    }

    private void assertIndividualDetails(CCDRespondent expected) {
        assertCommonDetails(expected);

        IndividualDetails individual = (IndividualDetails) actual;
        CCDParty expectedDetail = expected.getClaimantProvidedDetail();

        compare("title",
            expectedDetail.getTitle(),
            individual.getTitle());

        compare("dateOfBirth",
            expectedDetail.getDateOfBirth(),
            individual.getDateOfBirth());
    }

}
