package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;

import java.io.IOException;
import java.util.Optional;

public class EvidenceSerializer extends JsonSerializer<Optional<Evidence>> {

    private static final String VALUE = "value";

    @Override
    public void serialize(Optional<Evidence> evidence, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        Evidence data = evidence.orElse(null);
        data.getRows().forEach(evidenceRow ->
        {
            try {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeObjectField(VALUE, evidenceRow);
                jsonGenerator.writeEndObject();
            } catch (Exception e) {
                throw new MappingException("Unable to serialize evidence.", e);
            }
        });

        jsonGenerator.writeEndArray();
    }
}
