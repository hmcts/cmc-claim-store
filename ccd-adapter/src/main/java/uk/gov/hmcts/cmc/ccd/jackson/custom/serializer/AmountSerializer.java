package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

public class AmountSerializer extends JsonSerializer<Amount> {

    private static final String VALUE = "value";

    @Override
    public boolean isUnwrappingSerializer() {
        return true;
    }

    @Override
    public void serialize(Amount amount, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {


//        jsonGenerator.
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("amountType", amount.getType());
//        serializerProvider.defaultSerializeField("amountType", amount.getType(), jsonGenerator);

        if (amount instanceof AmountBreakDown) {
            jsonGenerator.writeArrayFieldStart("amountBreakDown");

            Map<String, AmountRow> rows = new HashMap<>();

            ((AmountBreakDown) amount).getRows().forEach(amountRow ->
            {
                try {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeObjectField(VALUE, amountRow);
                    jsonGenerator.writeEndObject();
                } catch (IOException e) {

                }
            });

            jsonGenerator.writeEndArray();

//            serializerProvider.defaultSerializeField("amountBreakDown", ((AmountBreakDown) amount).getRows(), jsonGenerator);

        }

        if (amount instanceof AmountRange) {
            Map<String, Object> ranges = new HashMap<>();

            jsonGenerator.writeObjectField("amountLowerValue", ((AmountRange) amount).getLowerValue());
            jsonGenerator.writeObjectField("amountHigherValue", ((AmountRange) amount).getHigherValue());
//            jsonGenerator.writeObjectField("amountLowerValue", ((AmountRange) amount).getLowerValue());
//            jsonGenerator.writeObjectField("amountHigherValue", ((AmountRange) amount).getHigherValue());
//            serializerProvider.defaultSerializeField("amountLowerValue", ((AmountRange) amount).getLowerValue(), jsonGenerator);
//            serializerProvider.defaultSerializeField("amountHigherValue", ((AmountRange) amount).getHigherValue(), jsonGenerator);

        }
        jsonGenerator.writeEndObject();

    }

    @Override
    public void serializeWithType(Amount amount, JsonGenerator gen,
                                  SerializerProvider provider, TypeSerializer typeSer)
        throws IOException, JsonProcessingException {

        WritableTypeId typeId = typeSer.typeId(amount, START_OBJECT);
        serialize(amount, gen, provider); // call your customized serialize method

    }
}
