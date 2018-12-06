package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

public class ResponseMixInTest {

    private JsonMapper processor = new JsonMapper(new CCDAdapterConfig().ccdObjectMapper());

    @Test
    public void shouldProcessFullDefenceResponseToCCDJson() {
        //given
        FullDefenceResponse fullDefenceResponse = SampleResponse.FullDefence.builder()
            .withStatementOfTruth(StatementOfTruth.builder()
                .signerName("Signer Name")
                .signerRole("Signer Role")
                .build()
            )
            .build();

        String json = processor.toJson(fullDefenceResponse);
        System.out.println(json);
//        FullDefenceResponse output = processor.fromJson(json, FullDefenceResponse.class);
//        String outputJson = processor.toJson(output);
//        Assert.assertThat(json, CoreMatchers.equalTo(outputJson));
    }

}
