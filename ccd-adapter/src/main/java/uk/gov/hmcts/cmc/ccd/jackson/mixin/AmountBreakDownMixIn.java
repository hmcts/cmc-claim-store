package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.AmountRow;

import java.util.List;

@SuppressWarnings("squid:S1610")
public abstract class AmountBreakDownMixIn extends AmountMixIn {

    @JsonProperty("rows")
    abstract List<AmountRow> getRows();
}
