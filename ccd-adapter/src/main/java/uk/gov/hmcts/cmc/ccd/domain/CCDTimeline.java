package uk.gov.hmcts.cmc.ccd.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CCDTimeline {
    private List<CCDCollectionElement<CCDTimelineEvent>> events;
}
