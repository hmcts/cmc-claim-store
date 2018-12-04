package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.hmcts.cmc.ccd.jackson.custom.deserializer.ListItemDeserializer;
import uk.gov.hmcts.cmc.ccd.jackson.custom.serializer.ListItemSerializer;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.util.List;

public abstract class AmountBreakDownMixIn {

    @JsonDeserialize(contentUsing = ListItemDeserializer.class)
    @JsonSerialize(contentUsing = ListItemSerializer.class)
    @JsonProperty("amountBreakDown")
    abstract List<AmountRow> getRows();
}
