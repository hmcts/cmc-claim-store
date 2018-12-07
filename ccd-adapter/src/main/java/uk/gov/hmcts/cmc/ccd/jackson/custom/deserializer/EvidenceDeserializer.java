package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType;

import java.io.IOException;
import java.util.ArrayList;

public class EvidenceDeserializer extends JsonDeserializer<Evidence> {
    private static final String VALUE = "value";

    @Override
    public Evidence deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        JsonNode productNode = jsonParser.getCodec().readTree(jsonParser);
        ArrayList<EvidenceRow> returnList = new ArrayList<>();
        if (productNode.isArray()) {
            for (JsonNode childNode : productNode) {
                JsonNode jsonNode = childNode.get(VALUE);
                EvidenceRow row = new EvidenceRow(EvidenceType.get(jsonNode.get("type").asText()), jsonNode.get("description").asText());
                returnList.add(row);
            }
        }
        return new Evidence(returnList);
    }
}
