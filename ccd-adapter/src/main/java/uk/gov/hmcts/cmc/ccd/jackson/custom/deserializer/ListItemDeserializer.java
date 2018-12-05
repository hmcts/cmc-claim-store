package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListItemDeserializer extends JsonDeserializer<List<JsonNode>> {

    public static final String VALUE = "value";

    @Override
    public List<JsonNode> deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        ArrayList<JsonNode> returnList = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode childNode : node) {
                returnList.add(childNode.get(VALUE));
            }
        }
        return returnList;
    }
}
