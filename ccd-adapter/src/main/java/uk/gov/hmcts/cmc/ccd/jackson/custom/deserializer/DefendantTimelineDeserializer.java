package uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

import java.io.IOException;
import java.util.ArrayList;


public class DefendantTimelineDeserializer extends JsonDeserializer<DefendantTimeline> {
    private static final String VALUE = "value";

    @Override
    public DefendantTimeline deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {

        JsonNode productNode = jsonParser.getCodec().readTree(jsonParser);
        ArrayList<TimelineEvent> returnList = new ArrayList<>();
        if (productNode.isArray()) {
            for (JsonNode childNode : productNode) {
                JsonNode jsonNode = childNode.get(VALUE);
                TimelineEvent row = new TimelineEvent(jsonNode.get("date").asText(), jsonNode.get("description").asText());
                returnList.add(row);
            }
        }
        return new DefendantTimeline(returnList, "");
    }
}
