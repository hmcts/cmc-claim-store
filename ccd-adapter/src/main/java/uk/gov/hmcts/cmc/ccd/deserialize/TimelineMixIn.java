package uk.gov.hmcts.cmc.ccd.deserialize;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.List;

public abstract class TimelineMixIn extends PartyMixIn {

    @JsonProperty("timeline")
    List<TimelineEvent> events;
}
