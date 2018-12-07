package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.io.IOException;

public class ResponseMixInTest {

    private final ObjectMapper objectMapper = new CCDAdapterConfig().ccdObjectMapper();
    private JsonMapper processor = new JsonMapper(objectMapper);

    @Test
    public void shouldSerialiseFullDefenceResponseToCCDJson() throws IOException {
        FullDefenceResponse fullDefenceResponse = SampleResponse.FullDefence.builder()
            .withStatementOfTruth(StatementOfTruth.builder()
                .signerName("Signer Name")
                .signerRole("Signer Role")
                .build()
            )
            .build();


        String json = processor.toJson(fullDefenceResponse);
        System.out.println(json);
//        String individualFullDefenceResponse = new ResourceReader()
//            .read("/serialization-samples/individual-full-defence-response.json");
//        JsonNode expected = objectMapper.readTree(individualFullDefenceResponse);
//        JsonNode result = objectMapper.readTree(json);
//        assertEquals(result, expected);
    }

}
