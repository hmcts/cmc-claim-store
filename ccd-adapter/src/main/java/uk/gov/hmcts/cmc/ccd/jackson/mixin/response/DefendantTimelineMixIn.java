package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.List;
import java.util.Optional;

public abstract class DefendantTimelineMixIn {

    @JsonProperty("Rows")
    abstract List<TimelineEvent> getRows();

    @JsonProperty("Comment")
    abstract Optional<String> getComment();
}
