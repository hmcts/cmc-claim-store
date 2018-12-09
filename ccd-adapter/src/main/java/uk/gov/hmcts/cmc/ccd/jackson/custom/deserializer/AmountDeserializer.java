package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AmountDeserializer extends StdDeserializer<Amount> {
    private Map<String, Class<? extends Amount>> registry =
        new HashMap<String, Class<? extends Amount>>();

    public AmountDeserializer()
    {
        super(Amount.class);
    }

    void registerAnimal(String uniqueAttribute,
                        Class<? extends Amount> animalClass)
    {
        registry.put(uniqueAttribute, animalClass);
    }


    @Override
    public Amount deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jsonParser);
        Class<? extends Amount> animalClass = null;
        Iterator<Map.Entry<String, JsonNode>> elementsIterator = root.fields();
        while (elementsIterator.hasNext())
        {
            Map.Entry<String, JsonNode> element=elementsIterator.next();
            String name = element.getKey();
            if (registry.containsKey(name))
            {
                animalClass = registry.get(name);
                break;
            }
        }
        if (animalClass == null) return null;
        return mapper.readValue(jsonParser, animalClass);

    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return typeDeserializer.deserializeTypedFromAny(p, ctxt);
    }
}
