package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import uk.gov.hmcts.cmc.custom.deserializer.ListItemDeserializer;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.util.List;

public abstract class AmountBreakDownMixIn {

    @JsonDeserialize(contentUsing = ListItemDeserializer.class)
    abstract List<AmountRow> getRows();
}
