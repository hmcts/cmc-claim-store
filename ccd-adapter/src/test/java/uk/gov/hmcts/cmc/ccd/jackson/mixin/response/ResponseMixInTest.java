package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

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
        String individualFullDefenceResponse = new ResourceReader()
            .read("/serialization-samples/individual-full-defence-response.json");
        JsonNode expected = objectMapper.readTree(individualFullDefenceResponse);

        String json = processor.toJson(fullDefenceResponse);
        JsonNode result = objectMapper.readTree(json);
        assertEquals(result, expected);
    }

}
