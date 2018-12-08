package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
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


        serializerProvider.defaultSerializeField("amountType", amount.getType(), jsonGenerator);

        if (amount instanceof AmountBreakDown) {
            serializerProvider.defaultSerializeField("amountBreakDown", ((AmountBreakDown) amount).getRows(), jsonGenerator);

        } else if (amount instanceof AmountRange) {
            serializerProvider.defaultSerializeField("amountLowerValue", ((AmountRange) amount).getLowerValue(), jsonGenerator);
            serializerProvider.defaultSerializeField("amountHigherValue", ((AmountRange) amount).getHigherValue(), jsonGenerator);

        }
    }
}
