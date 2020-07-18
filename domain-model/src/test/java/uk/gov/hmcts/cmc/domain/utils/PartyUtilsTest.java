package uk.gov.hmcts.cmc.domain.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PartyUtilsTest {

    @Test
    public void getTypeMapsPartyTypesCorrectly() {
        assertThat(PartyUtils.getType(SampleParty.builder().individual()))
            .isEqualTo(PartyUtils.INDIVIDUAL);

        assertThat(PartyUtils.getType(SampleParty.builder().company()))
            .isEqualTo(PartyUtils.ON_BEHALF_OF_A_COMPANY);

        assertThat(PartyUtils.getType(SampleParty.builder().organisation()))
            .isEqualTo(PartyUtils.ON_BEHALF_OF_AN_ORGANISATION);

        assertThat(PartyUtils.getType(SampleParty.builder().soleTrader()))
            .isEqualTo(PartyUtils.SOLE_TRADER_OR_SELF_EMPLOYED_PERSON);
    }

    @Test(expected = NotificationException.class)
    public void getTypeThrowsWhenPartyTypeUnknown() {
        PartyUtils.getType(new Party(UUID.randomUUID().toString(), null, null,
            null, null, null, null) {
        });
    }

    @Test(expected = NotificationException.class)
    public void getTypeTheirDetailsThrowsWhenPartyTypeUnknown() {
        PartyUtils.getType(new TheirDetails(UUID.randomUUID().toString(), null, null,
            null, null, null, null) {
        });
    }

    @Test
    public void getTypeMapsTheirDetailsPartyTypesCorrectly() {
        assertThat(PartyUtils.getType(SampleTheirDetails.builder().individualDetails()))
            .isEqualTo(PartyUtils.INDIVIDUAL);

        assertThat(PartyUtils.getType(SampleTheirDetails.builder().companyDetails()))
            .isEqualTo(PartyUtils.ON_BEHALF_OF_A_COMPANY);

        assertThat(PartyUtils.getType(SampleTheirDetails.builder().organisationDetails()))
            .isEqualTo(PartyUtils.ON_BEHALF_OF_AN_ORGANISATION);

        assertThat(PartyUtils.getType(SampleTheirDetails.builder().soleTraderDetails()))
            .isEqualTo(PartyUtils.SOLE_TRADER_OR_SELF_EMPLOYED_PERSON);

    }

    @Test
    public void getDefendantContactPersonReturnsContactPerson() {
        CompanyDetails companyDetails = SampleTheirDetails.builder().companyDetails();
        assertThat(PartyUtils.getContactPerson(companyDetails))
            .isEqualTo(companyDetails.getContactPerson());

        OrganisationDetails organisationDetails = SampleTheirDetails.builder().organisationDetails();
        assertThat(PartyUtils.getContactPerson(organisationDetails))
            .isEqualTo(organisationDetails.getContactPerson());
    }

    @Test
    public void getDefendantContactPersonReturnsEmptyOptionalWhenNotCompanyType() {
        IndividualDetails defendant = SampleTheirDetails.builder().individualDetails();
        assertThat(PartyUtils.getContactPerson(defendant))
            .isEqualTo(Optional.empty());
    }

    @Test
    public void getDefendantBusinessName() {
        SoleTraderDetails defendant = SampleTheirDetails.builder().soleTraderDetails();
        assertThat(PartyUtils.getBusinessName(defendant))
            .isEqualTo(defendant.getBusinessName());
    }

    @Test
    public void getDefendantBusinessNameReturnsEmptyOptionalWhenNotSoleTraderType() {
        IndividualDetails defendant = SampleTheirDetails.builder().individualDetails();
        assertThat(PartyUtils.getBusinessName(defendant))
            .isEqualTo(Optional.empty());
    }

    @Test
    public void getContactPerson() {
        Company company = SampleParty.builder().company();
        assertThat(PartyUtils.getContactPerson(company))
            .isEqualTo(company.getContactPerson());

        Organisation organisation = SampleParty.builder().organisation();
        assertThat(PartyUtils.getContactPerson(organisation))
            .isEqualTo(organisation.getContactPerson());
    }

    @Test
    public void getContactPersonReturnsEmptyOptionalWhenNotCompanyType() {
        Individual claimant = SampleParty.builder().individual();
        assertThat(PartyUtils.getContactPerson(claimant))
            .isEqualTo(Optional.empty());
    }

    @Test
    public void getBusinessNameWhenSoleTrader() {
        SoleTrader claimant = SampleParty.builder().soleTrader();
        assertThat(PartyUtils.getBusinessName(claimant))
            .isEqualTo(claimant.getBusinessName());
    }

    @Test
    public void getBusinessNameReturnsEmptyOptionalWhenAnIndividual() {
        Individual claimant = SampleParty.builder().individual();
        assertThat(PartyUtils.getBusinessName(claimant))
            .isEqualTo(Optional.empty());
    }

    @Test
    public void getClaimantDateOfBirth() {
        Individual claimant = SampleParty.builder().individual();
        assertThat(PartyUtils.claimantDateOfBirth(claimant))
            .isEqualTo(Optional.of(claimant.getDateOfBirth()));
    }

    @Test
    public void shouldReturnFalseForPartyIsNull() {
        //noinspection ConstantConditions
        assertThat(PartyUtils.isCompanyOrOrganisation(null)).isFalse();
    }

    @Test
    public void shouldReturnFalseForPartyIndividual() {
        Individual individual = SampleParty.builder().individual();
        assertThat(PartyUtils.isCompanyOrOrganisation(individual)).isFalse();
    }

    @Test
    public void shouldReturnFalseForPartySoleTrader() {
        SoleTrader soleTrader = SampleParty.builder().soleTrader();
        assertThat(PartyUtils.isCompanyOrOrganisation(soleTrader)).isFalse();
    }

    @Test
    public void shouldReturnTrueForPartyCompany() {
        Company company = SampleParty.builder().company();
        assertThat(PartyUtils.isCompanyOrOrganisation(company)).isTrue();
    }

    @Test
    public void shouldReturnTrueForPartyOrganisation() {
        Organisation organisation = SampleParty.builder().organisation();
        assertThat(PartyUtils.isCompanyOrOrganisation(organisation)).isTrue();
    }

    @Test
    public void shouldReturnFullNameFromSplitName() {
        assertThat(PartyUtils.fullNameFrom("Mr.", "Coffee", "McCoffee")).isEqualTo("Mr. Coffee McCoffee");
        assertThat(PartyUtils.fullNameFrom(null, "Coffee", "McCoffee")).isEqualTo("Coffee McCoffee");
    }
}
