package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.io.IOException;

public class ClaimSerializer extends JsonSerializer<Claim> {


    @Override
    public void serialize(Claim claim, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

    }

    private void serializeCCJIntoDefendant(Claim claim, JsonGenerator generator, SerializerProvider serializers){
    }

}
