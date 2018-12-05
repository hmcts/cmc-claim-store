package uk.gov.hmcts.cmc.claimstore.jackson.serialize;

import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.claimstore.config.JacksonConfiguration;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ClaimantResponseSerializationTest {

    private JsonMapper processor = new JsonMapper(new CCDAdapterConfig().ccdObjectMapper());

    @Test
    public void claimantResponseForDefendantPartAdmitWithImmediatePayment() {
        Claim claim = SampleClaim.builder().build();
        String json = processor.toJson(claim);
        System.out.println("claim");
        System.out.println(json);
        String claimantResponse = new ResourceReader().read("/jackson-serialization-samples/claimant-response.json");
        assertThat(json, isJson());
        //TODO : either this
        assertEquals(json,claimantResponse);
        //TODO: or if you want more granular testing this.
        assertThat(json, hasJsonPath("$.defendants[:1].value.asdfadsfasdfasdfasdf"));
        assertThat(json, hasJsonPath("$.defendants[:1].value.claimantResponse.claimantPaymentIntention"));
    }
}
