package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.List;

public abstract class TimelineMixIn extends PartyMixIn {

    List<TimelineEvent> events;
}
