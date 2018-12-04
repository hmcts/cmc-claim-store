package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.util.List;

public abstract class AmountBreakDownMixIn {

//    @JsonDeserialize(contentUsing = ListItemDeserializer.class)
    @JsonProperty("amountBreakDown")
    abstract List<AmountRow> getRows();
}
