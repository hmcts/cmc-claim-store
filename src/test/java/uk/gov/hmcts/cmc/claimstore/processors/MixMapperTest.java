package uk.gov.hmcts.cmc.claimstore.processors;

import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static org.junit.Assert.assertThat;

public class MixMapperTest {

    private JsonMapper processor = new JsonMapper(new CCDAdapterConfig().ccdObjectMapper());

    @Test
    public void shouldProcessIndividualToCCDJson() throws JSONException {
        //given
        Party individual = SampleParty.builder().individual();

        System.out.println(processor.toJson(individual));
    }

    @Test
    public void shouldProcessSoleTraderToCCDJson() throws JSONException {
        //given
        Party soleTrader = SampleParty.builder().soleTrader();

        System.out.println(processor.toJson(soleTrader));
    }

    @Test
    public void shouldProcessCompanyToCCDJson() throws JSONException {
        //given
        Party company = SampleParty.builder().company();

        String json = processor.toJson(company);
        System.out.println(json);

        Company output = processor.fromJson(json, Company.class);
        String outputJson = processor.toJson(output);
        assertThat(json, CoreMatchers.equalTo(outputJson));

//        assertThat(output,  CoreMatchers.is(company));

    }

    @Test
    public void shouldProcessOrganisationToCCDJson() throws JSONException {
        //given
        Party organisation = SampleParty.builder().organisation();

        String json = processor.toJson(organisation);
        System.out.println(json);

        Organisation output = processor.fromJson(json, Organisation.class);
        String outputJson = processor.toJson(output);

        assertThat(json, CoreMatchers.equalTo(outputJson));
//        assertThat(output,  CoreMatchers.is(organisation));
    }

    @Test
    public void shouldProcessIndividualDetailsToCCDJson() throws JSONException {
        //given
        TheirDetails individual = SampleTheirDetails.builder().individualDetails();

        System.out.println(processor.toJson(individual));
    }

    @Test
    public void shouldProcessSoleTraderDetailsToCCDJson() throws JSONException {
        //given
        TheirDetails soleTrader = SampleTheirDetails.builder().soleTraderDetails();

        System.out.println(processor.toJson(soleTrader));
    }

    @Test
    public void shouldProcessCompanyDetailsToCCDJson() throws JSONException {
        //given
        TheirDetails company = SampleTheirDetails.builder().companyDetails();

        System.out.println(processor.toJson(company));
    }

    @Test
    public void shouldProcessOrganisationDetailsToCCDJson() throws JSONException {
        //given
        OrganisationDetails organisation = SampleTheirDetails.builder().organisationDetails();

        String json = processor.toJson(organisation);
        System.out.println(json);

//        assertThat(processor.fromJson(json, Organisation.class)).isEqualTo(organisation);
    }

    @Test
    public void shouldProcessClaimDataToCCDJson() throws JSONException {
        //given
        ClaimData claimData = SampleClaimData.builder().build();

        String json = processor.toJson(claimData);
        System.out.println(json);

//        assertThat(processor.fromJson(json, Organisation.class)).isEqualTo(organisation);
    }

    @Test
    public void shouldProcessAmountBreakDownToCCDJson() throws JSONException {
        //given
        AmountBreakDown amountBreakDown = SampleAmountBreakdown.builder().build();

        String json = processor.toJson(amountBreakDown);
        System.out.println(json);

        AmountBreakDown output = processor.fromJson(json, AmountBreakDown.class);
        String outputJson = processor.toJson(output);
        System.out.println(amountBreakDown.toString());
        System.out.println(output.toString());

        assertThat(output,  CoreMatchers.is(amountBreakDown));
        assertThat(json, CoreMatchers.equalTo(outputJson));
    }


    @Test
    public void shouldProcessClaimToCCDJson() throws JSONException {
        //given
        Claim claim = SampleClaim.builder().withResponse(null).build();

        String json = processor.toJson(claim);
        System.out.println(json);

    }


}
