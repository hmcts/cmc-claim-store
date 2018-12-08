package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.util.List;

public abstract class AmountBreakDownMixIn extends AmountMixIn {

    @JsonProperty("amountBreakDown")
    abstract List<AmountRow> getRows();
}
