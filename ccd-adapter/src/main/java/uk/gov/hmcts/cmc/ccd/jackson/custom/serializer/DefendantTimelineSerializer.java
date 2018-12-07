package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

import java.io.IOException;
import java.util.Optional;

public class DefendantTimelineSerializer extends JsonSerializer<Optional<DefendantTimeline>> {

    @Override
    public boolean isUnwrappingSerializer() {
        return true;
    }

    @Override
    public void serialize(
        Optional<DefendantTimeline> optionalDefendantTimeline,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {

        DefendantTimeline defendantTimeline = optionalDefendantTimeline.orElse(null);

        String comment = defendantTimeline.getComment().orElse("");

        serializerProvider.defaultSerializeField(
            "defendantTimeLineComment",
            comment,
            jsonGenerator);

        serializerProvider.defaultSerializeField(
            "defendantTimeLineEvents",
            defendantTimeline.getEvents(),
            jsonGenerator);
    }
}
