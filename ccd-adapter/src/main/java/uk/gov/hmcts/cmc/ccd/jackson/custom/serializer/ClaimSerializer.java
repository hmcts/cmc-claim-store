package uk.gov.hmcts.cmc.ccd.jackson.custom.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.io.IOException;

public class ClaimSerializer extends JsonSerializer<Claim> {


    @Override
    public void serialize(Claim claim, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("submitterId", claim.getSubmitterId());
        jsonGenerator.writeStringField("externalId",claim.getExternalId());
        jsonGenerator.writeStringField("referenceNumber",claim.getReferenceNumber());

        jsonGenerator.writeStringField("reason",claim.getClaimData().getReason());
        //TODO check why this is not working
       // claim.getClaimData().getFeeCode().isPresent(feeCode -> jsonGenerator.writeStringField("feeCode",feeCode);
        if(claim.getClaimData().getFeeCode().isPresent()){
            jsonGenerator.writeStringField("feeCode",claim.getClaimData().getFeeCode().get());
        }
        //jsonGenerator.writeStringField("amountType",claim.getClaimData().getAmount().getType());
        jsonGenerator.writeObjectField("amountBreakDown", claim.getClaimData().getAmount());
        jsonGenerator.writeEndObject();
    }

    private void writeClaim(Claim claim, JsonGenerator jsonGenerator, SerializerProvider serializerProvider){

    }
}
