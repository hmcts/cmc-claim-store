package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

public class ListItemSerializer extends StdSerializer<List> {

    private final String idFieldName = "id";
    private final String valueFieldName = "value";

    public ListItemSerializer(Class<List> t) {
        super(t);
    }

    @Override
    public void serialize(List listItems, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        listItems.forEach(listItem -> {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField(idFieldName, StringUtils.EMPTY);
                jsonGenerator.writeObjectField(valueFieldName, listItem);
                jsonGenerator.writeEndObject();
            } catch (Exception gene) {
                throw new RuntimeException("Unable to serialize list items.");
            }
        });

        jsonGenerator.writeEndArray();
    }
}
