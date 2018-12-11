package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer.ClaimDeserializer;
import uk.gov.hmcts.cmc.ccd.jackson.custom.serializer.ClaimSerializer;
import uk.gov.hmcts.cmc.ccd.processors.JsonMapper;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.response.FullDefenceResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.io.IOException;

public class ResponseMixInTest {

    private final ObjectMapper objectMapper = new CCDAdapterConfig().ccdObjectMapper();
    private JsonMapper processor = new JsonMapper(objectMapper);

    private JsonMapper myMapper = new JsonMapper(new ObjectMapper().registerModule(
        new SimpleModule()
            .addSerializer(Claim.class, new ClaimSerializer())
            .addDeserializer(Claim.class, new ClaimDeserializer())
    ));


    @Test
    public void shouldSerialiseFullDefenceResponseToCCDClaim() throws IOException {

        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        String json = myMapper.toJson(claim);

        System.out.println(json);

//        FullDefenceResponse output = processor.fromJson(json, FullDefenceResponse.class);
//        System.out.println("===================");
//        System.out.println(output);
//        String individualFullDefenceResponse = new ResourceReader()
//            .read("/serialization-samples/individual-full-defence-response.json");
//        JsonNode expected = objectMapper.readTree(individualFullDefenceResponse);
//        JsonNode result = objectMapper.readTree(json);
//        assertEquals(result, expected);
    }

    @Test
    public void shouldDeserialiseFullDefenceResponseToCCDClaim() throws IOException {

        Claim claim = SampleClaim.getClaimWithFullDefenceNoMediation();

        String json = myMapper.toJson(claim);

        System.out.println(json);

        Claim output = myMapper.fromJson(json, Claim.class);
        System.out.println("==================");
        System.out.println(output);

//        FullDefenceResponse output = processor.fromJson(json, FullDefenceResponse.class);
//        System.out.println("===================");
//        System.out.println(output);
//        String individualFullDefenceResponse = new ResourceReader()
//            .read("/serialization-samples/individual-full-defence-response.json");
//        JsonNode expected = objectMapper.readTree(individualFullDefenceResponse);
//        JsonNode result = objectMapper.readTree(json);
//        assertEquals(result, expected);
    }


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
        FullDefenceResponse response = processor.fromJson(json, FullDefenceResponse.class);
        System.out.println("=========================");
        System.out.println(response);

    }
}
