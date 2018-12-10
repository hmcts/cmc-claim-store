package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ClaimSerializer extends JsonSerializer<Claim> {

    private final ObjectMapper processor;

    public ClaimSerializer() {
        processor = new CCDAdapterConfig().ccdObjectMapper();
    }

    @Override
    public void serialize(
        Claim claim,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {

        JsonNode claimAsJsonNode =
            processor.readTree(
                processor.writeValueAsString(claim)
            );

        moveDefendantResponse(claimAsJsonNode);

        moveCountryCourtJudgmentStruture(claimAsJsonNode);

        removeStructureFromClaim((ObjectNode) claimAsJsonNode);

        jsonGenerator.writeTree(claimAsJsonNode);
    }

    private void moveDefendantResponse(JsonNode claimAsJsonNode) {
        Optional.ofNullable(claimAsJsonNode.get("response")).ifPresent(
            response -> {
                ArrayNode defendants = (ArrayNode) claimAsJsonNode.get("defendants");
                ((ObjectNode) defendants.get(0).get("value")).putAll((ObjectNode) response);
            }
        );
    }

    private void removeStructureFromClaim(ObjectNode claimAsJsonNode) {

        List<String> fieldsToRemove = Arrays.asList("countyCourtJudgement", "features", "response", "amount");

        claimAsJsonNode.remove(fieldsToRemove);

    }

    private void moveCountryCourtJudgmentStruture(JsonNode claimAsJsonNode) {

        Optional.ofNullable(claimAsJsonNode.get("countyCourtJudgment")).ifPresent(
            countyCourtJudgement -> {
                ArrayNode defendants = (ArrayNode) claimAsJsonNode.get("defendants");
                ((ObjectNode) defendants.get(0)
                    .get("value"))
                    .putPOJO("countyCourtJudgement", countyCourtJudgement);
            }
        );

    }
}
