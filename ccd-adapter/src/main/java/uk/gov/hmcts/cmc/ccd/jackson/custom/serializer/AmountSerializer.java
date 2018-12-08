package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import uk.gov.hmcts.cmc.ccd.exception.MappingException;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

import java.io.IOException;

public class AmountSerializer extends JsonSerializer<Amount> {

    private static final String VALUE = "value";

    @Override
    public boolean isUnwrappingSerializer() {
        return true;
    }

    @Override
    public void serialize(Amount amount, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {


        serializerProvider.defaultSerializeField("type", amount.getType(), jsonGenerator);

        if (amount instanceof AmountBreakDown) {
            serializerProvider.defaultSerializeField("amountBreakDown", ((AmountBreakDown) amount).getRows(), jsonGenerator);
        super.serializeWithType(((AmountBreakDown) amount).getRows(), jsonGenerator, serializerProvider, new TypeSerializer() {
            @Override
            public TypeSerializer forProperty(BeanProperty beanProperty) {
                return null;
            }

            @Override
            public JsonTypeInfo.As getTypeInclusion() {
                return null;
            }

            @Override
            public String getPropertyName() {
                return null;
            }

            @Override
            public TypeIdResolver getTypeIdResolver() {
                return null;
            }

            @Override
            public WritableTypeId writeTypePrefix(JsonGenerator jsonGenerator, WritableTypeId writableTypeId) throws IOException {
                return null;
            }

            @Override
            public WritableTypeId writeTypeSuffix(JsonGenerator jsonGenerator, WritableTypeId writableTypeId) throws IOException {
                return null;
            }
        });

        } else if (amount instanceof AmountRange) {
            serializerProvider.defaultSerializeField("amountLowerValue", ((AmountRange) amount).getLowerValue(), jsonGenerator);
            serializerProvider.defaultSerializeField("amountHigherValue", ((AmountRange) amount).getHigherValue(), jsonGenerator);

        }
    }
}
