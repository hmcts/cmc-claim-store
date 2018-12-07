package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.cmc.domain.models.evidence.DefendantEvidence;

import java.io.IOException;
import java.util.Optional;

public class DefendantEvidenceSerializer extends JsonSerializer<Optional<DefendantEvidence>> {

    @Override
    public boolean isUnwrappingSerializer() {
        return true;
    }

    @Override
    public void serialize(
        Optional<DefendantEvidence> optionalDefendantEvidence,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {

        DefendantEvidence defendantEvidence = optionalDefendantEvidence.orElse(null);

        String comment = defendantEvidence.getComment().orElse("");

        serializerProvider.defaultSerializeField(
            "responseEvidenceComment",
            comment,
            jsonGenerator);

        serializerProvider.defaultSerializeField(
            "responseEvidenceRows",
            defendantEvidence.getRows(),
            jsonGenerator);
    }
}
