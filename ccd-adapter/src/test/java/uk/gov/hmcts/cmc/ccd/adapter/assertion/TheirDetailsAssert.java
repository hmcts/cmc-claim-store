package uk.gov.hmcts.cmc.ccd.adapter.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDParty;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.domain.models.legalrep.Representative;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.adapter.assertion.Assertions.assertThat;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.COMPANY;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.INDIVIDUAL;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.ORGANISATION;
import static uk.gov.hmcts.cmc.ccd.domain.CCDPartyType.SOLE_TRADER;

public class TheirDetailsAssert extends AbstractAssert<TheirDetailsAssert, TheirDetails> {

    public TheirDetailsAssert(TheirDetails party) {
        super(party, TheirDetailsAssert.class);
    }

    public TheirDetailsAssert isEqualTo(CCDRespondent respondent) {
        isNotNull();

        CCDParty partyDetails = respondent.getClaimantProvidedDetail();
        if (actual instanceof IndividualDetails) {
            if (!Objects.equals(INDIVIDUAL, partyDetails.getType())) {
                failWithMessage("Expected CCDRespondent.claimantProvidedType to be <%s> but was <%s>",
                    partyDetails.getType(), INDIVIDUAL);
            }
            assertIndividualDetails(respondent);
        }

        if (actual instanceof OrganisationDetails) {
            if (!Objects.equals(ORGANISATION, partyDetails.getType())) {
                failWithMessage("Expected CCDRespondent.claimantProvidedType to be <%s> but was <%s>",
                    partyDetails.getType(), ORGANISATION);
            }

            assertOrganisationDetails(respondent);
        }

        if (actual instanceof CompanyDetails) {
            if (!Objects.equals(COMPANY, partyDetails.getType())) {
                failWithMessage("Expected CCDRespondent.claimantProvidedType to be <%s> but was <%s>",
                    partyDetails.getType(), COMPANY);
            }

            assertCompanyDetails(respondent);
        }

        if (actual instanceof SoleTraderDetails) {
            if (!Objects.equals(SOLE_TRADER, partyDetails.getType())) {
                failWithMessage("Expected CCDRespondent.claimantProvidedType to be <%s> but was <%s>",
                    partyDetails.getType(), SOLE_TRADER);
            }
            assertSoleTraderDetails(respondent);
        }

        return this;
    }

    private void assertSoleTraderDetails(CCDRespondent respondent) {
        SoleTraderDetails actual = (SoleTraderDetails) this.actual;
        CCDParty claimantProvidedPartyDetail = respondent.getClaimantProvidedDetail();
        assertThat(actual.getAddress()).isEqualTo(claimantProvidedPartyDetail.getPrimaryAddress());

        actual.getTitle().ifPresent(title -> assertThat(claimantProvidedPartyDetail.getTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getName(), respondent.getClaimantProvidedPartyName())) {
            failWithMessage("Expected CCDRespondent.claimantProvidedName to be <%s> but was <%s>",
                respondent.getClaimantProvidedPartyName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), claimantProvidedPartyDetail.getEmailAddress())) {
            failWithMessage("Expected CCDRespondent.claimantProvidedEmail to be <%s> but was <%s>",
                claimantProvidedPartyDetail.getEmailAddress(), this.actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getBusinessName().orElse(null),
            claimantProvidedPartyDetail.getBusinessName())
        ) {
            failWithMessage("Expected CCDRespondent.claimantProvideBusinessName to be <%s> but was <%s>",
                claimantProvidedPartyDetail.getBusinessName(), actual.getBusinessName().orElse(null));
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(claimantProvidedPartyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, respondent));
    }

    private void assertCompanyDetails(CCDRespondent respondent) {
        CompanyDetails actual = (CompanyDetails) this.actual;
        CCDParty claimantProvidedPartyDetail = respondent.getClaimantProvidedDetail();

        assertThat(actual.getAddress()).isEqualTo(claimantProvidedPartyDetail.getPrimaryAddress());
        if (!Objects.equals(actual.getName(), respondent.getClaimantProvidedPartyName())) {
            failWithMessage("Expected CCDRespondent.claimantProvidedName to be <%s> but was <%s>",
                respondent.getClaimantProvidedPartyName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), claimantProvidedPartyDetail.getEmailAddress())) {
            failWithMessage("Expected claimantProvidedPartyDetail.getEmailAddress to be <%s> but was <%s>",
                claimantProvidedPartyDetail.getEmailAddress(), actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getContactPerson().orElse(null),
            claimantProvidedPartyDetail.getContactPerson())
        ) {
            failWithMessage("Expected claimantProvidedPartyDetail.getContactPerson to be <%s> but was <%s>",
                claimantProvidedPartyDetail.getContactPerson(), actual.getContactPerson().orElse(null));
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(claimantProvidedPartyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, respondent));
    }

    private void assertOrganisationDetails(CCDRespondent respondent) {
        OrganisationDetails actual = (OrganisationDetails) this.actual;
        CCDParty claimantProvidedPartyDetail = respondent.getClaimantProvidedDetail();

        assertThat(actual.getAddress()).isEqualTo(claimantProvidedPartyDetail.getPrimaryAddress());
        if (!Objects.equals(actual.getName(), respondent.getClaimantProvidedPartyName())) {
            failWithMessage("Expected CCDRespondent.claimantProvidedName to be <%s> but was <%s>",
                respondent.getClaimantProvidedPartyName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), claimantProvidedPartyDetail.getEmailAddress())) {
            failWithMessage("Expected CCDRespondent.claimantProvidedEmail to be <%s> but was <%s>",
                claimantProvidedPartyDetail.getEmailAddress(), actual.getEmail().orElse(null));
        }

        String contactPerson = actual.getContactPerson().orElse(null);
        if (!Objects.equals(contactPerson, claimantProvidedPartyDetail.getContactPerson())) {
            failWithMessage("Expected CCDRespondent.claimantProvidedContactPerson to be <%s> but was <%s>",
                claimantProvidedPartyDetail.getContactPerson(), contactPerson);
        }

        String companyHouseNumber = actual.getCompaniesHouseNumber().orElse(null);

        if (!Objects.equals(companyHouseNumber, claimantProvidedPartyDetail.getCompaniesHouseNumber())) {
            failWithMessage(
                "Expected CCDRespondent.claimantProvidedCompaniesHouseNumber to be <%s> but was <%s>",
                claimantProvidedPartyDetail.getCompaniesHouseNumber(), companyHouseNumber);
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(claimantProvidedPartyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, respondent));
    }

    private void assertIndividualDetails(CCDRespondent respondent) {
        IndividualDetails actual = (IndividualDetails) this.actual;
        CCDParty claimantProvidedPartyDetail = respondent.getClaimantProvidedDetail();

        assertThat(actual.getAddress()).isEqualTo(claimantProvidedPartyDetail.getPrimaryAddress());
        if (!Objects.equals(actual.getName(), respondent.getClaimantProvidedPartyName())) {
            failWithMessage("Expected CCDRespondent.claimantProvidedName to be <%s> but was <%s>",
                respondent.getClaimantProvidedPartyName(), actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), claimantProvidedPartyDetail.getEmailAddress())) {
            failWithMessage("Expected CCDRespondent.claimantProvidedEmail to be <%s> but was <%s>",
                claimantProvidedPartyDetail.getEmailAddress(), actual.getEmail().orElse(null));
        }
        actual.getDateOfBirth().ifPresent(dob ->
            assertThat(dob).isEqualTo(claimantProvidedPartyDetail.getDateOfBirth()));

        actual.getServiceAddress().ifPresent(address ->
            assertThat(claimantProvidedPartyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, respondent));
    }

    private void assertRepresentativeDetails(Representative representative, CCDRespondent respondent) {
        if (!Objects.equals(representative.getOrganisationName(),
            respondent.getClaimantProvidedRepresentativeOrganisationName())
        ) {
            failWithMessage("Expected Representative.organisationName to be <%s> but was <%s>",
                respondent.getClaimantProvidedRepresentativeOrganisationName(), representative.getOrganisationName());
        }

        assertThat(representative.getOrganisationAddress())
            .isEqualTo(respondent.getClaimantProvidedRepresentativeOrganisationAddress());

        representative.getOrganisationContactDetails().ifPresent(contactDetails -> {

            contactDetails.getDxAddress().ifPresent(dxAddress -> {
                if (!Objects.equals(dxAddress, respondent.getClaimantProvidedRepresentativeOrganisationDxAddress())) {
                    failWithMessage("Expected Representative.organisationDxAddress to be <%s> but was <%s>",
                        respondent.getClaimantProvidedRepresentativeOrganisationDxAddress(),
                        contactDetails.getDxAddress()
                    );
                }
            });

            contactDetails.getEmail().ifPresent(email -> {
                if (!Objects.equals(email, respondent.getClaimantProvidedRepresentativeOrganisationEmail())) {
                    failWithMessage("Expected Representative.organisationEmail to be <%s> but was <%s>",
                        respondent.getClaimantProvidedRepresentativeOrganisationEmail(), contactDetails.getEmail());
                }
            });

            contactDetails.getPhone().ifPresent(phoneNumber -> {
                if (!Objects.equals(phoneNumber, respondent.getClaimantProvidedRepresentativeOrganisationPhone())) {
                    failWithMessage("Expected Representative.organisationPhone to be <%s> but was <%s>",
                        respondent.getClaimantProvidedRepresentativeOrganisationPhone(), contactDetails.getPhone());
                }
            });
        });

    }
}
