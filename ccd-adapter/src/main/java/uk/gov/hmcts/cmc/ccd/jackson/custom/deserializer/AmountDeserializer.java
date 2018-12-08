package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

import java.io.IOException;
import java.math.BigDecimal;

public class AmountDeserializer extends JsonDeserializer<Amount> {
    @Override
    public Amount deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        JsonNode productNode = jsonParser.getCodec().readTree(jsonParser);
        String type = ((Amount) jsonParser.getParsingContext().getParent()
            .getCurrentValue()).getType();

        JsonNode amountType = productNode.get("amountType");

        switch (amountType.asText()){
            case "amountBreakDown":
                return null;
                default:
                    return null;
        }

    }
}
