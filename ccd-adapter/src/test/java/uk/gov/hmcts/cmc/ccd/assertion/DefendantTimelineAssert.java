package uk.gov.hmcts.cmc.ccd.assertion;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDTimelineEvent;
import uk.gov.hmcts.cmc.ccd.domain.response.CCDDefendantTimeline;
import uk.gov.hmcts.cmc.domain.models.TimelineEvent;
import uk.gov.hmcts.cmc.domain.models.response.DefendantTimeline;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class DefendantTimelineAssert extends AbstractAssert<DefendantTimelineAssert, DefendantTimeline> {

    public DefendantTimelineAssert(DefendantTimeline actual) {
        super(actual, DefendantTimelineAssert.class);
    }

    public DefendantTimelineAssert isEqualTo(CCDDefendantTimeline ccdTimeline) {
        isNotNull();

        if (!Objects.equals(actual.getEvents().size(), ccdTimeline.getEvents().size())) {
            failWithMessage("Expected DefendantTimeline.size to be <%s> but was <%s>",
                actual.getEvents().size(), ccdTimeline.getEvents().size());
        }

        if (!Objects.equals(actual.getComment().orElse(null), ccdTimeline.getComment())) {
            failWithMessage("Expected DefendantTimeline.comment to be <%s> but was <%s>",
                actual.getComment().orElse(null), ccdTimeline.getComment());
        }

        actual.getEvents()
            .forEach(event -> assertTimelineEvent(event, ccdTimeline.getEvents()));

        return this;
    }

    private void assertTimelineEvent(
        TimelineEvent actual,
        List<CCDCollectionElement<CCDTimelineEvent>> ccdTimeline
    ) {
        ccdTimeline.stream()
            .map(CCDCollectionElement::getValue)
            .filter(timelineEvent -> actual.getDate().equals(timelineEvent.getDate()))
            .findFirst()
            .ifPresent(event -> assertThat(actual).isEqualTo(event));
    }

}
