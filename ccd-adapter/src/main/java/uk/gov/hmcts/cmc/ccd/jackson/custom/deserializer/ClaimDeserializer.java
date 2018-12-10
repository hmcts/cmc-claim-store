package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.cmc.ccd.config.CCDAdapterConfig;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.io.IOException;
import java.util.Optional;

public class ClaimDeserializer extends JsonDeserializer<Claim> {

    private final ObjectMapper processor;

    public ClaimDeserializer() {
        processor = new CCDAdapterConfig().ccdObjectMapper();
    }

    @Override
    public Claim deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {

        JsonNode ccdClaimNode = jsonParser.getCodec().readTree(jsonParser);
        moveCCDStructureToClaim(ccdClaimNode);
        removeCCDElement(ccdClaimNode);

        Claim claim = processor.treeToValue(ccdClaimNode, Claim.class);
        return claim;
    }

    private void removeCCDElement(JsonNode ccdClaimNode) {
        Optional.ofNullable(ccdClaimNode.get("defendants")).ifPresent(
            defendants -> {
                JsonNode firstDefendantValue = (defendants).get(0).get("value");
                ((ObjectNode) firstDefendantValue).remove("countyCourtJudgement");
            }
        );
    }

    private void moveCCDStructureToClaim(JsonNode ccdClaimNode) {
        Optional.ofNullable(ccdClaimNode.get("defendants")).ifPresent(
            defendants -> {
                JsonNode firstDefendant = defendants.get(0).get("value");
                extractCountyCourtJudgement((ObjectNode) ccdClaimNode, firstDefendant);
                extractCountyDefendantResponse((ObjectNode) ccdClaimNode, firstDefendant);
            }
        );
    }

    private void extractCountyDefendantResponse(ObjectNode ccdClaimNode, JsonNode firstDefendant) {
        JsonNode countyCourtJudgment = firstDefendant.get("d");
        ccdClaimNode.putPOJO("countyCourtJudgement", countyCourtJudgment);
    }

    private void extractCountyCourtJudgement(ObjectNode ccdClaimNode, JsonNode firstDefendant) {
        JsonNode countyCourtJudgment = firstDefendant.get("countyCourtJudgement");
        ccdClaimNode.putPOJO("countyCourtJudgement", countyCourtJudgment);
    }
}
