package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.io.IOException;

public class AmountSerializer extends JsonSerializer {

    private static final String VALUE = "value";

    @Override
    public void serialize(Object amount, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartArray();
        if (amount instanceof AmountBreakDown) {
            ((AmountBreakDown) amount).getRows().forEach(row ->
            {
                try {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeObjectField(VALUE, row);
                    jsonGenerator.writeEndObject();
                } catch (Exception e) {
                    throw new MappingException("Unable to serialize evidence.", e);
                }
            });

            jsonGenerator.writeEndArray();
        }
    }
}
