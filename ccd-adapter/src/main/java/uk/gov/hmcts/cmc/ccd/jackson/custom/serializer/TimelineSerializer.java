package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.Timeline;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;

import java.io.IOException;
import java.util.Optional;

public class TimelineSerializer extends JsonSerializer<Optional<Timeline>> {

    private static final String VALUE = "value";

    @Override
    public void serialize(Optional<Timeline> evidence, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        Timeline data = evidence.orElse(null);
        data.getEvents().forEach(event ->
        {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField(VALUE, event);
                jsonGenerator.writeEndObject();
            } catch (Exception e) {
                throw new MappingException("Unable to serialize timeline.", e);
            }
        });

        jsonGenerator.writeEndArray();
    }
}
