package uk.gov.hmcts.cmc.claimstore.processors;

import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static org.junit.Assert.assertThat;

public class MixMapperTest {

    private JsonMapper processor = new JsonMapper(new CCDAdapterConfig().ccdObjectMapper());
    private JsonMapper defaultProcessor = new JsonMapper(new JacksonConfiguration().objectMapper());

    @Test
    public void shouldProcessIndividualToCCDJson() throws JSONException {
        //given
        Individual individual = SampleParty.builder().individual();

        String json = processor.toJson(individual);

        Individual output = processor.fromJson(json, Individual.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }

    @Test
    public void shouldProcessSoleTraderToCCDJson() throws JSONException {
        //given
        SoleTrader soleTrader = SampleParty.builder().soleTrader();

        String json = processor.toJson(soleTrader);

        SoleTrader output = processor.fromJson(json, SoleTrader.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }

    @Test
    public void shouldProcessCompanyToCCDJson() throws JSONException {
        //given
        Company company = SampleParty.builder().company();

        String json = processor.toJson(company);

        Company output = processor.fromJson(json, Company.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));

    }

    @Test
    public void shouldProcessOrganisationToCCDJson() throws JSONException {
        //given
        Organisation organisation = SampleParty.builder().organisation();

        String json = processor.toJson(organisation);

        Organisation output = processor.fromJson(json, Organisation.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }

    @Test
    public void shouldProcessIndividualDetailsToCCDJson() throws JSONException {
        //given
        IndividualDetails individual = SampleTheirDetails.builder().individualDetails();

        String json = processor.toJson(individual);

        IndividualDetails output = processor.fromJson(json, IndividualDetails.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }

    @Test
    public void shouldProcessSoleTraderDetailsToCCDJson() throws JSONException {
        //given
        SoleTraderDetails soleTrader = SampleTheirDetails.builder().soleTraderDetails();

        String json = processor.toJson(soleTrader);

        SoleTraderDetails output = processor.fromJson(json, SoleTraderDetails.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }

    @Test
    public void shouldProcessCompanyDetailsToCCDJson() throws JSONException {
        //given
        TheirDetails company = SampleTheirDetails.builder().companyDetails();

        String json = processor.toJson(company);

        TheirDetails output = processor.fromJson(json, TheirDetails.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }

    @Test
    public void shouldProcessOrganisationDetailsToCCDJson() throws JSONException {
        //given
        OrganisationDetails organisation = SampleTheirDetails.builder().organisationDetails();

        String json = processor.toJson(organisation);

        OrganisationDetails output = processor.fromJson(json, OrganisationDetails.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }

    @Test
    public void shouldProcessClaimDataToCCDJson() throws JSONException {
        //given
        ClaimData claimData = SampleClaimData.builder().build();

        String json = processor.toJson(claimData);
        System.out.println(json);

        ClaimData output = processor.fromJson(json, ClaimData.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }

    @Test
    public void shouldProcessAmountBreakDownToCCDJson() throws JSONException {
        //given
        AmountBreakDown amountBreakDown = SampleAmountBreakdown.builder().build();
        String json = processor.toJson(amountBreakDown);

        AmountBreakDown output = processor.fromJson(json, AmountBreakDown.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }


    @Test
    public void shouldProcessClaimToCCDJson() throws JSONException {
        //given
        Claim claim = SampleClaim.builder().withResponse(null).withFeatures(null).build();

        String json = processor.toJson(claim);
        System.out.println(json);

        Claim output = processor.fromJson(json, Claim.class);
        String outputJson = processor.toJson(output);
        System.out.println(outputJson);

        String defaultOne = defaultProcessor.toJson(claim);
        System.out.println(defaultOne);
        System.out.println(defaultProcessor.fromJson(defaultOne, Claim.class));

        assertThat(json, CoreMatchers.equalTo(outputJson));
    }
}
