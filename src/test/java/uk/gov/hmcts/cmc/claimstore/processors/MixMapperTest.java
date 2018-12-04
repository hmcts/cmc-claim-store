package uk.gov.hmcts.cmc.claimstore.processors;

import org.json.JSONException;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleParty;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleTheirDetails;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class MixMapperTest {

    private JsonMapper processor = new JsonMapper(new JacksonConfiguration().ccdObjectMapper());

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

        System.out.println(processor.toJson(company));
    }

    @Test
    public void shouldProcessOrganisationToCCDJson() throws JSONException {
        //given
        Party organisation = SampleParty.builder().organisation();

        String json = processor.toJson(organisation);
        System.out.println(json);

//        assertThat(processor.fromJson(json, Organisation.class)).isEqualTo(organisation);
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


}
