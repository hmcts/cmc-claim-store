package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

public abstract class AmountBreakDownMixIn {

    public AmountBreakDownMixIn() {
    }

    public class AmountRowSerializer extends StdSerializer<List> {

        public AmountRowSerializer() {
            this(null);
        }

        public AmountRowSerializer(Class<List> t) {
            super(t);
        }

        @Override
        public void serialize(List value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            ListIterator listIterator = value.listIterator();
            while (listIterator.hasNext()) {
                gen.writeObjectField("value", listIterator.next());
            }
            gen.writeEndObject();
        }
    }

    ;

    @JsonSerialize(contentUsing = AmountRowSerializer.class)
    abstract List<AmountRow> getRows();
}
