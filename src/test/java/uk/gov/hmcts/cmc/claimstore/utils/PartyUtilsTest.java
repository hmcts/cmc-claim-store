package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotificationException;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.party.Company;
import uk.gov.hmcts.cmc.claimstore.models.party.Individual;
import uk.gov.hmcts.cmc.claimstore.models.party.Organisation;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.models.party.SoleTrader;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleTheirDetails;

import java.util.Optional;

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
        PartyUtils.getType(new Party(null, null, null, null, null) {
        });
    }

    @Test(expected = NotificationException.class)
    public void getTypeTheirDetailsThrowsWhenPartyTypeUnknown() {
        PartyUtils.getType(new TheirDetails(null, null, null, null, null) {
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
        assertThat(PartyUtils.getDefendantContactPerson(companyDetails))
            .isEqualTo(companyDetails.getContactPerson());

        OrganisationDetails organisationDetails = SampleTheirDetails.builder().organisationDetails();
        assertThat(PartyUtils.getDefendantContactPerson(organisationDetails))
            .isEqualTo(organisationDetails.getContactPerson());
    }

    @Test
    public void getDefendantContactPersonReturnsEmptyOptionalWhenNotCompanyType() {
        IndividualDetails defendant = SampleTheirDetails.builder().individualDetails();
        assertThat(PartyUtils.getDefendantContactPerson(defendant))
            .isEqualTo(Optional.empty());
    }

    @Test
    public void getDefendantBusinessName() {
        SoleTraderDetails defendant = SampleTheirDetails.builder().soleTraderDetails();
        assertThat(PartyUtils.getDefendantBusinessName(defendant))
            .isEqualTo(defendant.getBusinessName());
    }

    @Test
    public void getDefendantBusinessNameReturnsEmptyOptionalWhenNotSoleTraderType() {
        IndividualDetails defendant = SampleTheirDetails.builder().individualDetails();
        assertThat(PartyUtils.getDefendantBusinessName(defendant))
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
    public void getBusinessName() {
        SoleTrader claimant = SampleParty.builder().soleTrader();
        assertThat(PartyUtils.getBusinessName(claimant))
            .isEqualTo(claimant.getBusinessName());
    }

    @Test
    public void getBusinessNameReturnsEmptyOptionalWhenNotSoleTraderType() {
        Individual claimant = SampleParty.builder().individual();
        assertThat(PartyUtils.getBusinessName(claimant))
            .isEqualTo(Optional.empty());
    }

}
