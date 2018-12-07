package uk.gov.hmcts.cmc.ccd.jackson.mixin.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.List;
import java.util.Optional;

public abstract class DefendantTimelineMixIn {

    @JsonProperty("defendantTimeLineEvents")
    abstract List<TimelineEvent> getEvents();

    @JsonIgnore
    abstract List<TimelineEvent> rows();

    @JsonProperty("defendantTimeLineComment")
    abstract Optional<String> getComment();
}
