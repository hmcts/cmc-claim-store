package uk.gov.hmcts.cmc.ccd.deprecated.domain.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.deprecated.domain.CCDTimelineEvent;

import java.util.List;

@Deprecated
@Value
@Builder
public class CCDDefendantTimeline {
    private List<CCDCollectionElement<CCDTimelineEvent>> events;
    private String comment;

    @JsonCreator
    public CCDDefendantTimeline(List<CCDCollectionElement<CCDTimelineEvent>> events, String comment) {
        this.events = events;
        this.comment = comment;
    }
}
