package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;

import java.io.IOException;
import java.util.List;

public class ListItemSerializer extends StdSerializer<List> {

    private static final String ID = "id";
    private static final String VALUE = "value";

    public ListItemSerializer(Class<List> t) {
        super(t);
    }

    @Override
    public void serialize(
        List listItems,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeStartArray();

        listItems.forEach(listItem -> {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField(ID, StringUtils.EMPTY);
                jsonGenerator.writeObjectField(VALUE, listItem);
                jsonGenerator.writeEndObject();
            } catch (Exception e) {
                throw new MappingException("Unable to serialize list items.", e);
            }
        });

        jsonGenerator.writeEndArray();
    }
}
