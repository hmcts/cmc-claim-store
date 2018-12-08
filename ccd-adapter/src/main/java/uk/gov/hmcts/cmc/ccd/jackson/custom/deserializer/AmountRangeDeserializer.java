package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;

import java.io.IOException;
import java.math.BigDecimal;

public class AmountRangeDeserializer extends JsonDeserializer<AmountRange> {
    @Override
    public AmountRange deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        JsonNode productNode = jsonParser.getCodec().readTree(jsonParser);
        return new AmountRange(BigDecimal.valueOf(productNode.get("amountLowerValue").asLong()),
            BigDecimal.valueOf(productNode.get("amountHigherValue").asLong())
        );
    }
}
