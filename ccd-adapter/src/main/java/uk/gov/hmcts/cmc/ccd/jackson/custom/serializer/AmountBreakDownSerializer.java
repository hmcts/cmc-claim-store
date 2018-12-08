package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.io.IOException;
import java.util.List;

public class AmountBreakDownSerializer extends Serializer<AmountBreakDown> {

    private static final String ID = "id";
    private static final String VALUE = "value";

    @Override
    public void serialize(AmountBreakDown amountBreakDown, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        amountBreakDown.getRows().forEach(listItem -> {
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
