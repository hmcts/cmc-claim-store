package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;

import java.io.IOException;
import java.util.Optional;

public class OptionalSerializer extends JsonSerializer<Optional> {

    @Override
    public void serialize(Optional optional, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        if (optional.isPresent()) {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObject(optional.get());
                jsonGenerator.writeEndObject();
            } catch (Exception e) {
                throw new MappingException("Unable to serialize optional.", e);
            }
        }

    }
}
