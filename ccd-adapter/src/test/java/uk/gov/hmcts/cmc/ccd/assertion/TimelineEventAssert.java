package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.Objects;

public class TimelineEventAssert extends AbstractAssert<TimelineEventAssert, TimelineEvent> {

    public TimelineEventAssert(TimelineEvent actual) {
        super(actual, TimelineEventAssert.class);
    }

    public TimelineEventAssert isEqualTo(CCDTimelineEvent ccdTimelineEvent) {
        isNotNull();

        if (!Objects.equals(actual.getDate(), ccdTimelineEvent.getDate())) {
            failWithMessage("Expected TimelineEvent.date to be <%s> but was <%s>",
                ccdTimelineEvent.getDate(), actual.getDate());
        }

        if (!Objects.equals(actual.getDescription(), ccdTimelineEvent.getDescription())) {
            failWithMessage("Expected TimelineEvent.description to be <%s> but was <%s>",
                ccdTimelineEvent.getDescription(), actual.getDescription());
        }

        return this;
    }

}
