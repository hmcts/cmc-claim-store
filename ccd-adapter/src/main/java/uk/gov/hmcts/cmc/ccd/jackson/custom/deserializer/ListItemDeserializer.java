package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListItemDeserializer extends JsonDeserializer<List> {

    public static final String VALUE = "value";

    @Override
    public List deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Class type;
        ArrayList returnList = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode childNode : node) {
                JsonNode valueNode = childNode.get(VALUE);
                JsonNode valueType = childNode.get("type");
                try {
                    type = (Class.forName(valueType.textValue()));
                } catch (ClassNotFoundException cls) {
                    throw new RuntimeException("Class not found exception " + valueType);
                }
                returnList.add(jsonParser.getCodec().treeToValue(valueNode, type));
            }
        }
        return returnList;
    }
}
