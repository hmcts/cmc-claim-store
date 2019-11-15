package uk.gov.hmcts.cmc.ccd.assertion;

import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.Optional;

public class TimelineEventAssert extends CustomAssert<TimelineEventAssert, TimelineEvent> {

    public TimelineEventAssert(TimelineEvent actual) {
        super("TimelineEvent", actual, TimelineEventAssert.class);
    }

    public TimelineEventAssert isEqualTo(CCDTimelineEvent expected) {
        isNotNull();

        compare("date",
            expected.getDate(),
            Optional.ofNullable(actual.getDate()));

        compare("description",
            expected.getDescription(),
            Optional.ofNullable(actual.getDescription()));

        return this;
    }

}
