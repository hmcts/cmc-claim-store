package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.evidence.Evidence;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceRow;
import uk.gov.hmcts.cmc.domain.models.evidence.EvidenceType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class AmountBreakDownDeserializer extends JsonDeserializer<AmountBreakDown> {
    private static final String VALUE = "value";

    @Override
    public AmountBreakDown deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        JsonNode productNode = jsonParser.getCodec().readTree(jsonParser);
        ArrayList<AmountRow> returnList = new ArrayList<>();
        JsonNode amountBreakDown = productNode.get("amountBreakDown");
        if (amountBreakDown.isArray()) {
            for (JsonNode childNode : amountBreakDown) {
                JsonNode jsonNode = childNode.get(VALUE);
                AmountRow row = new AmountRow(jsonNode.get("reason").asText(), BigDecimal.valueOf(jsonNode.get("amount").asLong()));
                returnList.add(row);
            }
        }
        return new AmountBreakDown(returnList);

    }
}
