package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimeline;
import uk.gov.hmcts.cmc.domain.models.Timeline;

import java.util.Objects;

public class TimelineAssert extends AbstractAssert<TimelineAssert, Timeline> {

    public TimelineAssert(Timeline actual) {
        super(actual, TimelineAssert.class);
    }

    public TimelineAssert isEqualTo(CCDTimeline ccdTimeline) {
        isNotNull();

        if (!Objects.equals(actual.getEvents().size(), ccdTimeline.getEvents().size())) {
            failWithMessage("Expected Timeline.size to be <%s> but was <%s>",
                actual.getEvents().size(), ccdTimeline.getEvents().size());
        }

        return this;
    }

}
