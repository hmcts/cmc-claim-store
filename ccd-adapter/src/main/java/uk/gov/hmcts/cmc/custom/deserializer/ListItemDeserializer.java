package uk.gov.hmcts.cmc.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListItemDeserializer extends JsonDeserializer<List<JsonNode>> {

    @Override
    public List<JsonNode> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        ArrayList<JsonNode> retunList = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode childNode : node) {
                retunList.add(childNode.get("value"));
            }
        }
        return retunList;
    }
}
