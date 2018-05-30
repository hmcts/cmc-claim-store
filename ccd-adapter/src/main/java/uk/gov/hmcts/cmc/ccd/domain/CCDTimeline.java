package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CCDTimeline {
    private List<CCDCollectionElement<CCDTimelineEvent>> events;

    @JsonCreator
    CCDTimeline(List<CCDCollectionElement<CCDTimelineEvent>> events) {
        this.events = events;
    }
}
