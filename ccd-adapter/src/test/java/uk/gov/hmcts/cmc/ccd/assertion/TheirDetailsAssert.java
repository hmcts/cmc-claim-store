package uk.gov.hmcts.cmc.ccd.assertion;

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
import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;
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

        CCDParty partyDetails = respondent.getApplicantProvidedDetails();
        if (actual instanceof IndividualDetails) {
            if (!Objects.equals(INDIVIDUAL, partyDetails.getType())) {
                failWithMessage("Expected CCDRespondent.applicantProvidedType to be <%s> but was <%s>",
                    partyDetails.getType(), INDIVIDUAL);
            }
            assertIndividualDetails(respondent);
        }

        if (actual instanceof OrganisationDetails) {
            if (!Objects.equals(ORGANISATION, partyDetails.getType())) {
                failWithMessage("Expected CCDRespondent.applicantProvidedType to be <%s> but was <%s>",
                    partyDetails.getType(), ORGANISATION);
            }

            assertOrganisationDetails(respondent);
        }

        if (actual instanceof CompanyDetails) {
            if (!Objects.equals(COMPANY, partyDetails.getType())) {
                failWithMessage("Expected CCDRespondent.applicantProvidedType to be <%s> but was <%s>",
                    partyDetails.getType(), COMPANY);
            }

            assertCompanyDetails(respondent);
        }

        if (actual instanceof SoleTraderDetails) {
            if (!Objects.equals(SOLE_TRADER, partyDetails.getType())) {
                failWithMessage("Expected CCDRespondent.applicantProvidedType to be <%s> but was <%s>",
                    partyDetails.getType(), SOLE_TRADER);
            }
            assertSoleTraderDetails(respondent);
        }

        return this;
    }

    private void assertSoleTraderDetails(CCDRespondent respondent) {
        SoleTraderDetails actual = (SoleTraderDetails) this.actual;
        CCDParty applicantProvidedPartyDetail = respondent.getApplicantProvidedDetails();
        assertThat(actual.getAddress()).isEqualTo(applicantProvidedPartyDetail.getPrimaryAddress());

        actual.getTitle().ifPresent(title -> assertThat(applicantProvidedPartyDetail.getTitle()).isEqualTo(title));

        if (!Objects.equals(actual.getName(), respondent.getApplicantProvidedPartyName())) {
            failWithMessage("Expected CCDRespondent.applicantProvidedName to be <%s> but was <%s>",
                respondent.getApplicantProvidedPartyName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), applicantProvidedPartyDetail.getEmailAddress())) {
            failWithMessage("Expected CCDRespondent.applicantProvidedEmail to be <%s> but was <%s>",
                applicantProvidedPartyDetail.getEmailAddress(), this.actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getBusinessName().orElse(null),
            applicantProvidedPartyDetail.getBusinessName())
        ) {
            failWithMessage("Expected CCDRespondent.applicantProvideBusinessName to be <%s> but was <%s>",
                applicantProvidedPartyDetail.getBusinessName(), actual.getBusinessName().orElse(null));
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(applicantProvidedPartyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, respondent));
    }

    private void assertCompanyDetails(CCDRespondent respondent) {
        CompanyDetails actual = (CompanyDetails) this.actual;
        CCDParty applicantProvidedPartyDetail = respondent.getApplicantProvidedDetails();

        assertThat(actual.getAddress()).isEqualTo(applicantProvidedPartyDetail.getPrimaryAddress());
        if (!Objects.equals(actual.getName(), respondent.getApplicantProvidedPartyName())) {
            failWithMessage("Expected CCDRespondent.applicantProvidedName to be <%s> but was <%s>",
                respondent.getApplicantProvidedPartyName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), applicantProvidedPartyDetail.getEmailAddress())) {
            failWithMessage("Expected applicantProvidedPartyDetail.getEmailAddress to be <%s> but was <%s>",
                applicantProvidedPartyDetail.getEmailAddress(), actual.getEmail().orElse(null));
        }

        if (!Objects.equals(actual.getContactPerson().orElse(null),
            applicantProvidedPartyDetail.getContactPerson())
        ) {
            failWithMessage("Expected applicantProvidedPartyDetail.getContactPerson to be <%s> but was <%s>",
                applicantProvidedPartyDetail.getContactPerson(), actual.getContactPerson().orElse(null));
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(applicantProvidedPartyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, respondent));
    }

    private void assertOrganisationDetails(CCDRespondent respondent) {
        OrganisationDetails actual = (OrganisationDetails) this.actual;
        CCDParty applicantProvidedPartyDetail = respondent.getApplicantProvidedDetails();

        assertThat(actual.getAddress()).isEqualTo(applicantProvidedPartyDetail.getPrimaryAddress());
        if (!Objects.equals(actual.getName(), respondent.getApplicantProvidedPartyName())) {
            failWithMessage("Expected CCDRespondent.applicantProvidedName to be <%s> but was <%s>",
                respondent.getApplicantProvidedPartyName(), this.actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), applicantProvidedPartyDetail.getEmailAddress())) {
            failWithMessage("Expected CCDRespondent.applicantProvidedEmail to be <%s> but was <%s>",
                applicantProvidedPartyDetail.getEmailAddress(), actual.getEmail().orElse(null));
        }

        String contactPerson = actual.getContactPerson().orElse(null);
        if (!Objects.equals(contactPerson, applicantProvidedPartyDetail.getContactPerson())) {
            failWithMessage("Expected CCDRespondent.applicantProvidedContactPerson to be <%s> but was <%s>",
                applicantProvidedPartyDetail.getContactPerson(), contactPerson);
        }

        String companyHouseNumber = actual.getCompaniesHouseNumber().orElse(null);

        if (!Objects.equals(companyHouseNumber, applicantProvidedPartyDetail.getCompaniesHouseNumber())) {
            failWithMessage(
                "Expected CCDRespondent.applicantProvidedCompaniesHouseNumber to be <%s> but was <%s>",
                applicantProvidedPartyDetail.getCompaniesHouseNumber(), companyHouseNumber);
        }

        actual.getServiceAddress().ifPresent(address ->
            assertThat(applicantProvidedPartyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, respondent));
    }

    private void assertIndividualDetails(CCDRespondent respondent) {
        IndividualDetails actual = (IndividualDetails) this.actual;
        CCDParty applicantProvidedPartyDetail = respondent.getApplicantProvidedDetails();

        assertThat(actual.getAddress()).isEqualTo(applicantProvidedPartyDetail.getPrimaryAddress());
        if (!Objects.equals(actual.getName(), respondent.getApplicantProvidedPartyName())) {
            failWithMessage("Expected CCDRespondent.applicantProvidedName to be <%s> but was <%s>",
                respondent.getApplicantProvidedPartyName(), actual.getName());
        }

        if (!Objects.equals(actual.getEmail().orElse(null), applicantProvidedPartyDetail.getEmailAddress())) {
            failWithMessage("Expected CCDRespondent.applicantProvidedEmail to be <%s> but was <%s>",
                applicantProvidedPartyDetail.getEmailAddress(), actual.getEmail().orElse(null));
        }
        actual.getDateOfBirth().ifPresent(dob ->
            assertThat(dob).isEqualTo(applicantProvidedPartyDetail.getDateOfBirth()));

        actual.getServiceAddress().ifPresent(address ->
            assertThat(applicantProvidedPartyDetail.getCorrespondenceAddress()).isEqualTo(address)
        );

        actual.getRepresentative()
            .ifPresent(representative -> assertRepresentativeDetails(representative, respondent));
    }

    private void assertRepresentativeDetails(Representative representative, CCDRespondent respondent) {
        if (!Objects.equals(representative.getOrganisationName(),
            respondent.getApplicantProvidedRepresentativeOrganisationName())
        ) {
            failWithMessage("Expected Representative.organisationName to be <%s> but was <%s>",
                respondent.getApplicantProvidedRepresentativeOrganisationName(), representative.getOrganisationName());
        }

        assertThat(representative.getOrganisationAddress())
            .isEqualTo(respondent.getApplicantProvidedRepresentativeOrganisationAddress());

        representative.getOrganisationContactDetails().ifPresent(contactDetails -> {

            contactDetails.getDxAddress().ifPresent(dxAddress -> {
                if (!Objects.equals(dxAddress, respondent.getApplicantProvidedRepresentativeOrganisationDxAddress())) {
                    failWithMessage("Expected Representative.organisationDxAddress to be <%s> but was <%s>",
                        respondent.getApplicantProvidedRepresentativeOrganisationDxAddress(),
                        contactDetails.getDxAddress()
                    );
                }
            });

            contactDetails.getEmail().ifPresent(email -> {
                if (!Objects.equals(email, respondent.getApplicantProvidedRepresentativeOrganisationEmail())) {
                    failWithMessage("Expected Representative.organisationEmail to be <%s> but was <%s>",
                        respondent.getApplicantProvidedRepresentativeOrganisationEmail(), contactDetails.getEmail());
                }
            });

            contactDetails.getPhone().ifPresent(phoneNumber -> {
                if (!Objects.equals(phoneNumber, respondent.getApplicantProvidedRepresentativeOrganisationPhone())) {
                    failWithMessage("Expected Representative.organisationPhone to be <%s> but was <%s>",
                        respondent.getApplicantProvidedRepresentativeOrganisationPhone(), contactDetails.getPhone());
                }
            });
        });

    }
}
