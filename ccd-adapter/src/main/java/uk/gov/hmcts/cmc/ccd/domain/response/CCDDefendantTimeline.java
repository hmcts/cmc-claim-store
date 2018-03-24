package uk.gov.hmcts.cmc.ccd.domain.response;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;

import java.util.List;

@Value
@Builder
public class CCDDefendantTimeline {
    private List<CCDCollectionElement<CCDTimelineEvent>> events;
    private String comment;
}
